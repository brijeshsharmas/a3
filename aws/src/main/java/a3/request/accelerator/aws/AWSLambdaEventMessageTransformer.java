package a3.request.accelerator.aws;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent.KinesisEventRecord;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNSRecord;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cloud.model.db.AttributeElement;
import cloud.model.db.DBStorageType;
import cloud.model.message.CloudMessage;
import cloud.model.nosql.NoSQLItem;
import cloud.model.request.CloudRequest;
import cloud.model.request.CloudResponse;
import cloud.request.accelerator.CloudRequestTransformer;
import cloud.service.CloudLogger;
import cloud.service.CloudRestAPIService.ContentType;
import cloud.service.CloudServiceException;
import cloud.service.CloudServiceUtil;

import static cloud.service.CloudRestAPIService.Key.*;

/**
 * @author <b>Brijesh Sharma</b><br>
 * This class is an implementation of {@link CloudRequestTransformer} interface for AWS Cloud Provider. This class
 * performs below transformations<br>
 * 1. Transform <b>AWS Lambda Event</b> Object Transformation To <b>{@link CloudMessageRequest}</b> object<br>
 * 2. Transform <b>{@link CloudMessageResponse}</b> object to <b>AWS-API Gateway Response in JSON Format</b>. Please refer 
 * class {@link APIGatewayResponse} for API-Gateway Response format. 
 *
 */
public class AWSLambdaEventMessageTransformer implements CloudRequestTransformer {
	
	protected CloudLogger logger = null;
	private String name = getClass().getSimpleName();

	/** ---------------------CONSTRUCTORS--------------------------------**/
	public AWSLambdaEventMessageTransformer() {	}
	public AWSLambdaEventMessageTransformer(CloudLogger logger) { this.logger = logger; }

	/** ----------------------Override CloudMessageTransformer-------------------------------**/
	@Override
	public CloudRequest transform(Object object) {
		if (object instanceof SQSEvent) return createCloudServiceMessage((SQSEvent)object);
		if (object instanceof DynamodbEvent) return createCloudServiceMessage((DynamodbEvent)object);
		if (object instanceof KinesisEvent) return createCloudServiceMessage((KinesisEvent)object);
		if (object instanceof S3Event) return createCloudServiceMessage((S3Event)object);
		if (object instanceof SNSEvent) return createCloudServiceMessage((SNSEvent)object);
		if (object instanceof APIGatewayProxyRequestEvent) return createCloudServiceMessage((APIGatewayProxyRequestEvent)object);
		if (object instanceof APIGatewayResponse) return createCloudServiceMessage((APIGatewayResponse)object);
		if(object instanceof Map<?, ?>)  return createCloudServiceMessage(((Map<?, ?>)object));
		
		throw new CloudServiceException("RequestTransformation", name + " Transformation Error: Object " + object.getClass() + " is NOT currently supported by " + name); 
	}
	@Override
	public Object transformResponse(List<CloudResponse> listCloudResponses) {
		
		return null;
		
	}
	@Override
	public Object transform(CloudResponse response) {
		
		ObjectNode root = JsonNodeFactory.instance.objectNode();
		ObjectNode body = JsonNodeFactory.instance.objectNode();
		root.set(BODY.getText(), body);
		
		if(CloudServiceUtil.isNumeric(response.getCode()))
			root.put(STATUS_CODE.getText(), Integer.parseInt(response.getCode()));
		else
			root.put(STATUS_CODE.getText(), response.getCode());
		
		Map<String, Object> mapHeaders = new HashMap<String, Object>();
		mapHeaders.put(CONTENT_TYPE.getText(), ContentType.JSON.getText());
		String jsonHeader =  CloudServiceUtil.transformObjectToJsonString(mapHeaders);
		root.set(HEADERS.getText(), CloudServiceUtil.transformObjectToJsonNode(jsonHeader));
		
		ObjectNode responseBody = CloudServiceUtil.buildJsonBody(response);
		body.set(CloudResponse.getResponseJsonTagName(), responseBody);
		if(response.getCloudRequest() != null)
			body.set(CloudRequest.getRequestJsonTagName(), CloudServiceUtil.buildJsonBody(response.getCloudRequest()));
		
		return root.toString();
	
	}
	
	@Override
	public void setLogger(CloudLogger logger) {	this.logger = logger;}
	
	/** ---------------------Transform AWS Lambda Event Object Into CloudMessageRequest------------------------------**/
	private CloudRequest createCloudServiceMessage(Map<?, ?> queryMap) {
		logger.printInfo("Creating CloudRequest Object From Map [" + queryMap + "]", name);
		//printQueryMap(queryMap, getLogger());
		CloudRequest request = new CloudRequest();
		if (queryMap == null) return request;
		
		int counter = 0;
		for (Map.Entry<?, ?> entry: queryMap.entrySet()) {
			if(entry == null || entry.getKey() == null || entry.getValue() == null) continue;
			
			if(entry.getValue() instanceof Map<?, ?>)
				populateCloudRequestWithQueryMap(request, ((Map<?, ?>)entry.getValue()), ++counter);
			else 
				request.addKeyValue(entry.getKey().toString(), entry.getValue().toString());	
		}
		
		logger.printInfo("Cloud Request Populated Key-Map Is [" + request.getKeyValueMap() + "]", name);
		return request;
	}
	private void populateCloudRequestWithQueryMap(CloudRequest request, Map<?, ?> queryMap, int counter) {
		//Safety net to avoid infinite loop
		if (counter > 5) {
			logger.printWarn("Recursive Counter To Populate Query Map Has Reached Max Limit Of 5, Aborting Any Further Population Of Query Map ", name);
			return;
		}
		
		for(Map.Entry<?, ?> entry: queryMap.entrySet()) {
			if(entry == null || entry.getKey() == null || entry.getValue() == null) continue;
			
			if (entry.getValue() instanceof Map<?, ?>) 
				populateCloudRequestWithQueryMap(request, ((Map<?, ?>)entry.getValue()), ++counter);
			else {
				String key = entry.getKey().toString();
				request.addKeyValue(key, entry.getValue().toString());
			}
		}
	}
	//CloudMessageRequest.Message = SQS Body In String Format
	private CloudRequest createCloudServiceMessage(SQSEvent event) {
		printInfo("Creating CloudRequest from SQS Event " + event);
		CloudRequest request = new CloudRequest();
		if (event == null) return request;
		
		List <SQSMessage> records = event.getRecords();
		List<CloudMessage> listMessage = new ArrayList<CloudMessage>();
		printInfo("Total Number of Messages in the SQS event are " + records.size());
		for (SQSMessage record: records) 
			listMessage.add(new CloudMessage( record.getMessageId(), 
					record.getBody()).withNativeObject(record));	
		request.setObject(listMessage);
		return request;
	}
	//CloudMessageRequest.Message = Kinesis Message in ByteBuffer format
	private CloudRequest createCloudServiceMessage(KinesisEvent event) {
		printInfo("Creating CloudRequest from Kinesis Event " + event);
		CloudRequest request = new CloudRequest();
		if (event == null) return request;
		
		List <KinesisEventRecord> records = event.getRecords();
		List<CloudMessage> listMessage = new ArrayList<CloudMessage>();
		printInfo("Total Number of Messages in the Kinesis event are " + records.size());
		for (KinesisEventRecord record: records) 
			listMessage.add( new CloudMessage( record.getKinesis().getPartitionKey(), 
					record.getKinesis().getData().toString()).withNativeObject(record));				
		request.setObject(listMessage);
		return request;
	}
	//CloudMessageRequest.Message = S3 Bucket Name in String format
	private CloudRequest createCloudServiceMessage(S3Event event) {
		printInfo("Creating CloudRequests from S3 Event " + event);
		CloudRequest request = new CloudRequest();
		if (event == null) return request;
		
		List <S3EventNotificationRecord> records = event.getRecords();
		printInfo("Total Number of Messages in the S3 event are " + records.size());
		List<CloudMessage> listMessage = new ArrayList<CloudMessage>();
		for (S3EventNotificationRecord record: records) {
			CloudMessage requestEntry = new CloudMessage(record.getS3().getObject().getKey(), 
					record.getS3().getBucket().getName()).withNativeObject(record);
			requestEntry.addKeyValue(CloudRequest.Property.USER_IDENTITY.name(), record.getUserIdentity().getPrincipalId());
			listMessage.add(requestEntry);	
		}
		request.setObject(listMessage);
		return request;
	}
	//CloudMessageRequest.Message = SNS Body In String Format
	private CloudRequest createCloudServiceMessage(SNSEvent event) {
		printInfo("Creating CloudRequests from SNS Event " + event);
		CloudRequest request = new CloudRequest();
		if (event == null) return request;
		
		List<CloudMessage> listMessage = new ArrayList<CloudMessage>();
		for(SNSRecord entry: event.getRecords()) 
			listMessage.add( new CloudMessage(entry.getSNS().getMessageId(),entry.getSNS().getMessage())
					.withKeyValue(CloudMessage.Key.SUBJECT.name(), entry.getSNS().getSubject()));
		printInfo("Total Number of Messages in the SNS event are " + listMessage.size());
		request.setObject(listMessage);
		return request;
	}
	//CloudMessageRequest.Message = POJOAWSAPIGatewayJSONResponse Body In String JSON Format
	private CloudRequest createCloudServiceMessage(APIGatewayResponse event) {
		printInfo("Creating CloudRequests from APIGatewayResponse Event " + event);
		if (event == null) return new CloudRequest();
		
		CloudRequest request = new CloudRequest(event.getStatusCode()+"", event.getBody());
		request.setNativeObject(event);
		
		return request;
	}
	//CloudMessageRequest.Message = APIGatewayProxyRequestEvent Body In String JSON Format
	private CloudRequest createCloudServiceMessage(APIGatewayProxyRequestEvent event) {
		printInfo("Creating CloudRequests from APIProxy Event " + event);
		if (event == null)return new CloudRequest();

		String body = event.getBody();
		printInfo("Body in APIGatewayRequest Event Object is " + body);
		return new CloudRequest ("Body", body).withNativeObject(event);
	}
	//CloudMessageRequest.Message = CloudMessageRequestEntry instance
	private CloudRequest createCloudServiceMessage(DynamodbEvent event) {
		printInfo("Creating CloudRequests from DynamoDBStream Event " + event);
		CloudRequest request = new CloudRequest();
		if (event == null) return request;

		List<NoSQLItem> listNoSQLItem = new ArrayList<NoSQLItem>();
		for(DynamodbStreamRecord  entry: event.getRecords()) {
			Map<String, AttributeValue> nextStreamRecordKeys = entry.getDynamodb().getKeys();
			Map<String, AttributeValue> nextStreamRecordAttributes = entry.getDynamodb().getNewImage();
			if (nextStreamRecordKeys == null || nextStreamRecordAttributes == null) continue;
			
			NoSQLItem item = new NoSQLItem();
			for (Map.Entry<String, AttributeValue> nextKey: nextStreamRecordKeys.entrySet()) {
				if (item.getPartitionKey() == null)
					item = item.withPrimaryKey(nextKey.getKey(), getDynamoDBDataValueFromAttributeValue(nextKey.getValue()));
				else
					item = item.withSortKey(nextKey.getKey(), getDynamoDBDataValueFromAttributeValue(nextKey.getValue()));
			}
			for (Map.Entry<String, AttributeValue> nextAttribute: nextStreamRecordAttributes.entrySet()) {
				AttributeElement attrDefinition = new AttributeElement(DBStorageType.NOSQL, nextAttribute.getKey(), 
						getDynamoDBDataValueFromAttributeValue(nextAttribute.getValue()));
				item = item.withAttribute(attrDefinition);
			}
			
			item.setEventType(entry.getEventName());
			listNoSQLItem.add(item);
		}
		request.setObject(listNoSQLItem);
		return request;
	}
	
	private Object getDynamoDBDataValueFromAttributeValue(AttributeValue value) {
		 if (value.getS() != null) return value.getS();
		 if (value.getN() != null) return new Integer(value.getN());
		 if (value.getSS() != null) return value.getSS();
		 if (value.getNS() != null) return value.getNS();
		 if (value.getBS() != null) return value.getBS();
		 if (value.getM() != null) return value.getM();
		 if (value.getL() != null) return value.getL();
		 if (value.getNULL() != null) return value.getNULL();
		 if (value.getBOOL() != null) return value.getBOOL();
		 return null;
	}
	private void printInfo(String message) {
		if (logger != null)
			logger.printInfo(message, name);
	}
	public CloudLogger getLogger() { return logger; }
}
