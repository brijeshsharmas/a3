package a3.service.aws;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Index;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputExceededException;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.RequestLimitExceededException;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.dynamodbv2.model.UpdateItemResult;
import com.amazonaws.services.stepfunctions.builder.internal.validation.ValidationException;

import cloud.model.db.AttributeElement;
import cloud.model.db.DBStorageType;
import cloud.model.db.KeySchema;
import cloud.model.nosql.NoSQLItem;
import cloud.model.nosql.NoSQLKeyType;
import cloud.model.request.CloudResponse;
import cloud.model.request.CloudResponses;
import cloud.service.CloudLogger;
import cloud.service.CloudNoSQLService;
import cloud.service.CloudServiceException;

import static a3.service.aws.AWSServiceFactory.Service.*;
import static cloud.request.accelerator.CloudRequestAccelerator.Config.*;
import static cloud.service.CloudService.Property.*;
import static cloud.service.CloudService.StatusCode.*;
/**
 * @author Brijesh Sharma
 * {@link CloudNoSQLService} implementation for No-SQL DB Service DynamoDB by AWS Cloud Provider. The caller of this service DO-NOT need to specify
 * primary key name and date types while creating {@link NoSQLItem} item. The class always maintain table key-schema information during intialization or
 * re-initialization
 *
 */
public class AWSDynamoDBService implements CloudNoSQLService {

	private Table table = null;
	private CloudLogger logger = null;
	private String strTableName, strPartitionKey, strSortKey;
	private KeySchema tableKeySchema = null;
	private String name = getClass().getSimpleName();
	private static final String KEY_TYPE_PARTITION = "HASH";
	private static final String KEY_TYPE_SORT = "RANGE";
	private static final int MAX_RETRY_COUNT = 5;
	private String serviceExceptionOperation = CLOUD_SERVICE_OPERATION.getText().
			replaceFirst(CLOUD_PROVIDER_SUBSITUTOR.getText(), "AWS").
			replaceFirst(SERVICE_NAME_SUBSITUTOR.getText(), DYNAMODB.name());
	
	/*---------------------Constructors----------------------------------*/
	public AWSDynamoDBService(Properties props){ initialize(props); }
	
	/*---------------------{@link CloudNoSQLService} method implementation----------------------------------*/
	@Override
	public CloudResponse add(NoSQLItem noSQLDBItem) { 
		Item item = transformNoSQLDBItemToAWSDynamoDBAddItem(noSQLDBItem);
		return addWithRetry(item, 0, MAX_RETRY_COUNT); 
	}
	
	@Override
	public CloudResponses add(List<NoSQLItem> request) {
		CloudResponses response = new CloudResponses();
		for(NoSQLItem entry: request) 
			response.addResponse(add(entry));
		//TO-DO abort operation for remaining set
		return response;
	}

	@Override
	public CloudResponse update(NoSQLItem noSQLDBItem) {
		UpdateItemSpec updateItemSpec = transformNoSQLDBItemToAWSDynamoDBUpdateItem(noSQLDBItem);
		return updateWithRetry(updateItemSpec, 0, MAX_RETRY_COUNT); 
	}
	
	@Override
	public CloudResponses update(List<NoSQLItem> listItems) {
		CloudResponses response = new CloudResponses();
		for(NoSQLItem entry: listItems) 
			response.addResponse(update(entry));
		//TO-DO abort operation for remaining set
		return response;
	}
	
	@Override
	public CloudResponse delete(NoSQLItem item) {
		CloudResponse response = new CloudResponse();
		if (strSortKey == null && strPartitionKey != null)
			table.deleteItem(strPartitionKey , item.getAttributeValue(strPartitionKey));
		else if (strPartitionKey != null && strSortKey != null)
			table.deleteItem(strPartitionKey , item.getAttributeValue(strPartitionKey), strSortKey, item.getAttributeValue(strSortKey));
		response.setProcessedStatus(true);
		return response;
	}

	@Override
	public boolean clearAll() {
		printInfo("Emptying Table " + getTableName() + ". KeySchema[PartitionKey=" + strPartitionKey + ", SortKey=" + strSortKey + "]");
		Iterator<Item> iterator = table.scan().iterator();
		while (iterator.hasNext()) {
			Item next = iterator.next();
			if (strSortKey == null && strPartitionKey != null)
				table.deleteItem(strPartitionKey , next.get(strPartitionKey));
			else if (strPartitionKey != null && strSortKey != null)
				table.deleteItem(strPartitionKey , next.get(strPartitionKey), strSortKey, next.get(strSortKey));
		}   
		return true;
	}
	
	@Override
	public List<NoSQLItem> getByIndex(String name, Object value) {
		List<NoSQLItem> response = new ArrayList<NoSQLItem>();
		Index index = table.getIndex(name);
		QuerySpec spec = new QuerySpec()
			    .withKeyConditionExpression(name + " = :v_one")
			    .withValueMap(new ValueMap()
			        .with(":v_one", value));

		ItemCollection<QueryOutcome> items = index.query(spec);
		Iterator<Item> itemIterator = items.iterator();
		while (itemIterator.hasNext()) {
		    Item item = itemIterator.next();
		    Iterable<Map.Entry<String, Object>> attr = item.attributes();
		    NoSQLItem requestItem = new NoSQLItem();
		    for (Map.Entry<String, Object> entry: attr) 
		    	requestItem = requestItem.withAttribute(entry.getKey(), entry.getValue());
	    	response.add(requestItem);
		}
		return response;
	}
	
	@Override
	public List<NoSQLItem> get(NoSQLItem noSQLDBItem) {
		List<NoSQLItem> response = new ArrayList<NoSQLItem>();
		
		QuerySpec spec = transformNoSQLDBItemToAWSDynamoDBQuerySpec(noSQLDBItem);
		ItemCollection<QueryOutcome> queryOutcome = table.query(spec);
		Iterator<Item> itemIterator = queryOutcome.iterator();
		while (itemIterator.hasNext()) {
			Item item = itemIterator.next();
		    Iterable<Map.Entry<String, Object>> attr = item.attributes();
		    NoSQLItem requestItem = new NoSQLItem()
	    			.withKeySchema(noSQLDBItem.getKeySchema());
		    for (Map.Entry<String, Object> entry: attr) 
		    	requestItem = requestItem.withAttribute(entry.getKey(), entry.getValue());
	    	response.add(requestItem);
		}
		
		return response;
	}
	@Override
	public NoSQLItem getByPrimaryKeysValue(Object partitionKeyValue, Object sortKeyValue) {
		NoSQLItem requestItem = new NoSQLItem()
				.withPrimaryKey(strPartitionKey, partitionKeyValue);
		 if (sortKeyValue != null)
			 requestItem.withSortKey(strSortKey, sortKeyValue);
		
		List<NoSQLItem> response = get(requestItem);
		for(NoSQLItem responseItem: response)
			return responseItem;
		
		return null;
	}
	@Override
	public List<NoSQLItem> getAll() {
		List<NoSQLItem> response = new ArrayList<NoSQLItem>();
		
		ItemCollection<ScanOutcome> selectAll = table.scan();
		Iterator<Item> iterator = selectAll.iterator();
		while (iterator.hasNext()) {
			Item item = iterator.next();
			Iterable<Map.Entry<String, Object>> attr = item.attributes();
			NoSQLItem requestItem = new NoSQLItem()
		    			.withKeySchema(tableKeySchema);
			    for (Map.Entry<String, Object> entry: attr) 
			    	requestItem = requestItem.withAttribute(entry.getKey(), entry.getValue());
		    	response.add(requestItem);
		}   
		return response;
	}
	
	@Override
	public NoSQLItem getAllAttributes() {
		NoSQLItem item = new NoSQLItem().withKeySchema(tableKeySchema);
		ItemCollection<ScanOutcome> selectAll = table.scan();
		Iterator<Item> iterator = selectAll.iterator();
		while (iterator.hasNext()) {
			Item nextItem = iterator.next();
			Iterable<Map.Entry<String, Object>> attr = nextItem.attributes();
			    for (Map.Entry<String, Object> entry: attr) {
			    	if (item.getAttributeValue(entry.getKey()) == null) {
			    		item = item.withAttribute(entry.getKey(), entry.getValue());
			    	}
			    }
		}   
		return item;
	}
	@Override
	public String getCloudSRN() { return strTableName; }

	@Override
	public boolean delete() {
		table.delete();
		return true;
	}
	
	@Override
	public void setLogger(CloudLogger logger) { this.logger = logger; }
	
	/**------------------Helper Methods Begins Here----------------------------------------------------------------------*/
	private void initialize(Properties props) {
		strTableName = props.getProperty(SERVICE_ARNS.getText());
		if (strTableName == null) throw new CloudServiceException(serviceExceptionOperation, 
				"Could not find service ARN in the property argument");

		DynamoDB dynamoDB = new DynamoDB(AmazonDynamoDBClientBuilder.standard().build());
		table = dynamoDB.getTable(strTableName);
	
		resetKeySchema();
	}
	
	/**This method introspect DynamoDB table and reset Partition and Sort Key details*/
	private void resetKeySchema( ) {
		strPartitionKey = null;strSortKey = null; tableKeySchema = new KeySchema();
	
		List<KeySchemaElement> schema =  table.describe().getKeySchema();
		List <AttributeDefinition> listDefinition = table.describe().getAttributeDefinitions();
		for (KeySchemaElement element: schema) {
			if (element.getKeyType().equalsIgnoreCase(KEY_TYPE_PARTITION)) {
				strPartitionKey = element.getAttributeName();
				tableKeySchema.addKey(DBStorageType.NOSQL, strPartitionKey, getAWSDataTypeForAttribute(listDefinition, strPartitionKey), 
						NoSQLKeyType.PARTITION_KEY.getType());			
			} 
			else if (element.getKeyType().equalsIgnoreCase(KEY_TYPE_SORT)) {
				strSortKey = element.getAttributeName();
				tableKeySchema.addKey(DBStorageType.NOSQL, strSortKey, getAWSDataTypeForAttribute(listDefinition, strSortKey), NoSQLKeyType.SORT_KEY.getType());	
			}
		}
	}
	
	private String getAWSDataTypeForAttribute(List <AttributeDefinition> listDefinition, String attributeName ) {
		for (AttributeDefinition next: listDefinition) {
			if(next.getAttributeName().equalsIgnoreCase(attributeName))
				return next.getAttributeType();
		}
		return null;
	}
	
	/**This method will keep trying to commit  {@link #noSQLDBItem} to {@link #intMaxRetryAttempt} times*/
	private CloudResponse addWithRetry(Item item, int intCurrentCounter, final int intMaxRetryAttempt) {
		try {
			PutItemOutcome outcome = table.putItem(item);
			PutItemResult result = outcome.getPutItemResult();
			return buildCloudResponseForPassedItem(item, result.getSdkResponseMetadata().getRequestId(), outcome);
		
		}catch(ValidationException | ConditionalCheckFailedException exception) {
			exception.printStackTrace();
			return buildCloudResponseForFailedItem(item, DATA_ISSUE, exception.getMessage());	
		
		}catch (ProvisionedThroughputExceededException | RequestLimitExceededException exception) {
			exception.printStackTrace();
			return retry(item, intCurrentCounter++, intMaxRetryAttempt);
		
		}catch(ResourceNotFoundException exception) {
			exception.printStackTrace();
			return buildCloudResponseForFailedItem(item, OUTAGE, exception.getMessage());	
		
		}catch (Exception exception) {
			exception.printStackTrace();
			return buildCloudResponseForFailedItem(item, OUTAGE, exception.getMessage());
		}
	}
	/**This method will keep trying to commit  {@link #noSQLDBItem} to {@link #intMaxRetryAttempt} times*/
	private CloudResponse updateWithRetry(UpdateItemSpec updateItemSpec, int intCurrentCounter, final int intMaxRetryAttempt) {
		try {
			UpdateItemOutcome outcome = table.updateItem(updateItemSpec);
			printInfo("Item Updated Succesfully. Updated Item =" + outcome.getItem().toJSONPretty());
			UpdateItemResult result = outcome.getUpdateItemResult();
			return buildCloudResponseForPassedItem(updateItemSpec, result.getSdkResponseMetadata().getRequestId(), outcome);
		
		}catch(ValidationException | ConditionalCheckFailedException exception) {
			exception.printStackTrace();
			return buildCloudResponseForFailedItem(updateItemSpec, DATA_ISSUE, exception.getMessage());	
		
		}catch (ProvisionedThroughputExceededException | RequestLimitExceededException exception) {
			exception.printStackTrace();
			return retry(updateItemSpec, intCurrentCounter++, intMaxRetryAttempt);
		
		}catch(ResourceNotFoundException exception) {
			exception.printStackTrace();
			return buildCloudResponseForFailedItem(updateItemSpec, OUTAGE, exception.getMessage());	
		
		}catch (Exception exception) {
			exception.printStackTrace();
			return buildCloudResponseForFailedItem(updateItemSpec, OUTAGE, exception.getMessage());
		}
	}
	private CloudResponse retry(Item item, int intCurrentCounter, final int intMaxRetryAttempt) {
		if (intCurrentCounter >= intMaxRetryAttempt)
			return transformAWSDynamoDBItemToNoSQLDBItem(item).buildCloudResponse()
					.withProcessedStatus(false)
					.withCode(ABORTED)
					.withMessage("Aborting DynanoDB Add-Item: Maximum number " + 
							intMaxRetryAttempt + "Retried ");
		return addWithRetry(item, intCurrentCounter, intMaxRetryAttempt);
	}
	private CloudResponse retry(UpdateItemSpec updateItemSpec, int intCurrentCounter, final int intMaxRetryAttempt) {
		if (intCurrentCounter >= intMaxRetryAttempt)
			return transformAWSDynamoDBItemToNoSQLDBItem(updateItemSpec).buildCloudResponse()
					.withProcessedStatus(false)
					.withCode(ABORTED)
					.withMessage("Aborting DynanoDB Add-Item: Maximum number " + 
							intMaxRetryAttempt + "Retried ");
		return updateWithRetry(updateItemSpec, intCurrentCounter, intMaxRetryAttempt);
	}
	
	/** ----------{@link CloudResponse} builder methods ----------------*/
	private CloudResponse buildCloudResponseForFailedItem(Item item, String strErrorCode, String errMessage) {
		return transformAWSDynamoDBItemToNoSQLDBItem(item).buildCloudResponse()
				.withProcessedStatus(false)
				.withCode(strErrorCode)
				.withShortMessage(INTERNAL_ERROR.name())
				.withMessage(errMessage);
	}
	private CloudResponse buildCloudResponseForPassedItem(Item item, String responseMsgId, Object nativeResult) {
		return transformAWSDynamoDBItemToNoSQLDBItem(item).buildCloudResponse()
				.withProcessedStatus(true)
				.withId(responseMsgId)
				.withNativeObject(nativeResult);
	}
	private CloudResponse buildCloudResponseForFailedItem(UpdateItemSpec updateItemSpec, String strErrorCode, String errMessage) {
		return transformAWSDynamoDBItemToNoSQLDBItem(updateItemSpec).buildCloudResponse()
				.withProcessedStatus(false)
				.withCode(strErrorCode)
				.withShortMessage(INTERNAL_ERROR.name())
				.withMessage(errMessage);
	}
	private CloudResponse buildCloudResponseForPassedItem(UpdateItemSpec updateItemSpec, String responseMsgId, Object nativeResult) {
		return transformAWSDynamoDBItemToNoSQLDBItem(updateItemSpec).buildCloudResponse()
				.withProcessedStatus(true)
				.withId(responseMsgId)
				.withNativeObject(nativeResult);
	}
	
	/**-----------------------------------Transformation methods ------------------------------------------------***/
	/**This method transform {@link NoSQLItem} to AWS {@link Item}*/
	private Item transformNoSQLDBItemToAWSDynamoDBAddItem(NoSQLItem noSQLDBItem) {
		Item item = null;
		if (strSortKey == null) //No SORT Key provided
			item = new Item().withPrimaryKey(strPartitionKey, noSQLDBItem.getAttributeValue(strPartitionKey));
		else 
			item = new Item().withPrimaryKey(strPartitionKey, noSQLDBItem.getAttributeValue(strPartitionKey),
						strSortKey, noSQLDBItem.getAttributeValue(strSortKey));
		for (Map.Entry<String, AttributeElement> attrElement: noSQLDBItem.getAttributeElements().entrySet()) 
			item = item.with(attrElement.getKey(), attrElement.getValue().getDataValue());
		
		return item;
	}
	
	/**This method transform {@link NoSQLItem} to AWS {@link Item}*/
	private QuerySpec transformNoSQLDBItemToAWSDynamoDBQuerySpec(NoSQLItem noSQLDBItem) {
		String keyConditionExpression = strPartitionKey + " = :v_" + strPartitionKey;
		if (noSQLDBItem.getSortKey() !=null)
			keyConditionExpression += " and " + strSortKey + " = :v_" + strSortKey;
		
		ValueMap valueMap = new ValueMap();
		String filterConditionExpression = null;
		for (Map.Entry<String, AttributeElement> next: noSQLDBItem.getAttributeElements().entrySet()) {
			valueMap = valueMap.with(":v_" + next.getValue().getName(), next.getValue().getDataValue());
			if (next.getValue().getName().equals(strPartitionKey)
					|| (strSortKey !=null && next.getValue().getName().equals(strSortKey))) continue;
			if (filterConditionExpression == null)
				filterConditionExpression = next.getValue().getName() + " " + next.getValue().getConditionOerator().getExpression() 
					+ " :v_" + next.getValue().getName();
			else
				filterConditionExpression += " and " + next.getValue().getName() + " " + next.getValue().getConditionOerator().getExpression() 
						+ " :v_" + next.getValue().getName();
		}
		QuerySpec querySpec = new QuerySpec().withKeyConditionExpression(keyConditionExpression)
				.withFilterExpression(filterConditionExpression);
		if(valueMap.size() > 0) querySpec.withValueMap(valueMap);
		printInfo("Creating AWS DynanomoDB Query Specs With Key Condition [" + keyConditionExpression + "], Filter Condition[" 
				+ filterConditionExpression + "], Value Map[" + valueMap + "]");
		return querySpec;
	}
	/**This method transform {@link NoSQLItem} to AWS {@link Item}*/
	private UpdateItemSpec transformNoSQLDBItemToAWSDynamoDBUpdateItem(NoSQLItem noSQLDBItem) {
		UpdateItemSpec updateItemSpec = null;
		if(strSortKey != null)
			updateItemSpec = new UpdateItemSpec().withPrimaryKey(strPartitionKey, noSQLDBItem.getAttributeValue(strPartitionKey), 
					strSortKey, noSQLDBItem.getAttributeValue(strSortKey));
		else
			updateItemSpec = new UpdateItemSpec().withPrimaryKey(strPartitionKey, noSQLDBItem.getAttributeValue(strPartitionKey));
		
		ValueMap valueMap = new ValueMap();
		String updateExpression = null;
		for (Map.Entry<String, AttributeElement> next: noSQLDBItem.getAttributeElements().entrySet()) {
			if (next.getValue().getName().equals(strPartitionKey)
					|| (strSortKey !=null && next.getValue().getName().equals(strSortKey))) continue;
			valueMap = valueMap.with(":v_" + next.getValue().getName(), next.getValue().getDataValue());
			if (updateExpression == null)
				updateExpression = "set " + next.getValue().getName() + " = :v_" + next.getValue().getName();
			else
				updateExpression += " , " + next.getValue().getName() + " = :v_" + next.getValue().getName();
		}
		updateItemSpec = updateItemSpec.withUpdateExpression(updateExpression)
		.withValueMap(valueMap)
		.withReturnValues(ReturnValue.UPDATED_NEW);

		printInfo("Creating AWS DynanomoDB Update Query Specs With Update Expression [" 
				+ updateExpression + "], Value Map[" + valueMap + "]");
		return updateItemSpec;
	}
	/**This method transform AWS {@link Item} to {@link NoSQLItem}*/
	private NoSQLItem transformAWSDynamoDBItemToNoSQLDBItem(Item item) {
		//TO-DO transform with proper data type
		NoSQLItem noSQLDBItem = new NoSQLItem().withPrimaryKey(strPartitionKey, null);
		if (strSortKey != null)
			noSQLDBItem = noSQLDBItem.withSortKey(strSortKey, null);
		
		for (Map.Entry<String, Object> attrElement: item.asMap().entrySet()) 
			noSQLDBItem = noSQLDBItem.withAttribute(attrElement.getKey(), attrElement.getValue());
		
		return noSQLDBItem;
	}
	private NoSQLItem transformAWSDynamoDBItemToNoSQLDBItem(UpdateItemSpec updateItemSpec) {
		//TO-DO transform with proper data type
		NoSQLItem noSQLDBItem = new NoSQLItem().withPrimaryKey(strPartitionKey, null);
		if (strSortKey != null)
			noSQLDBItem = noSQLDBItem.withSortKey(strSortKey, null);
		
		for (Map.Entry<String, Object> attrElement: updateItemSpec.getValueMap().entrySet()) 
			noSQLDBItem = noSQLDBItem.withAttribute(attrElement.getKey(), attrElement.getValue());
		
		return noSQLDBItem;
	}
	private String getTableName() {return strTableName; 	}
	private void printInfo(String str) {	
		if(logger != null) logger.printInfo(str, name);
	}

}
