/**
 * 
 */
package a3.service.aws;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kinesis.model.PutRecordResult;
import com.amazonaws.services.kinesis.model.PutRecordsRequest;
import com.amazonaws.services.kinesis.model.PutRecordsRequestEntry;
import com.amazonaws.services.kinesis.model.PutRecordsResult;
import com.amazonaws.services.kinesis.model.PutRecordsResultEntry;

import cloud.model.message.CloudMessage;
import cloud.model.request.CloudRequest;
import cloud.model.request.CloudResponse;
import cloud.model.request.CloudResponses;
import cloud.service.CloudLogger;
import cloud.service.CloudMessageService;
import cloud.service.CloudServiceException;
import cloud.service.CloudServiceUtil;

import java.nio.ByteBuffer;
import java.util.*;

import static a3.service.aws.AWSServiceFactory.Service.*;
import static cloud.request.accelerator.CloudRequestAccelerator.Config.*;
import static cloud.service.CloudService.Property.*;

/**
 * @author <b>Brijesh Sharma</b><br>
 * This class is an implementation of {@link CloudMessageService) interface for AWS cloud provider service named Kinesis Data Stream
 *
 */
public class AWSKinesisDataStreamService implements CloudMessageService {

	private AmazonKinesis kinesisClient = null;
	private String name = getClass().getSimpleName();
	private String kinesisARN = null;
	private CloudLogger logger = null;
	private String serviceExceptionOperation = CLOUD_SERVICE_OPERATION.getText().
			replaceFirst(CLOUD_PROVIDER_SUBSITUTOR.getText(), "AWS").
			replaceFirst(SERVICE_NAME_SUBSITUTOR.getText(), KINESIS_DATA_STREAM.name());
	
	/**----------------------------------------------------Constructor-------------------------------------------------*/
	public AWSKinesisDataStreamService(Properties props) { initialize(props); }

	
	/**----------------------------------------------------{@link CloudMessageService} Implementation-------------------------------------------------*/
	@Override
	public CloudResponse publishMessage(CloudMessage cloudMessage) {
		
		try {
			PutRecordResult result = kinesisClient.putRecord(kinesisARN, 
					ByteBuffer.wrap(CloudServiceUtil.serialize(cloudMessage.getBody())), cloudMessage.getId());
			logger.printInfo( "Succesfully Posted Message [Id=" + cloudMessage.getId() + ", Body=" + cloudMessage.getBody() + "]", name);
			return buildCloudResponse(cloudMessage, result);
			
		}catch (Exception e) {
			e.printStackTrace();
			return buildCloudResponse(cloudMessage, e);
		}	
	}

	/***This method uses {@link CloudMessage#getId()} as PartitionKey*/
	@Override
	public CloudResponses publishMessagesInBatch(List<CloudMessage> listCloudMessages)  {
		
		PutRecordsRequest putRequest = new PutRecordsRequest(); 
		putRequest.setStreamName(kinesisARN);
		List<PutRecordsRequestEntry> putRequestRecords = transformFromCloudMessageToPutRecordsRequestEntry(listCloudMessages);
		putRequest.setRecords(putRequestRecords);
		PutRecordsResult putRecordResult =  kinesisClient.putRecords(putRequest);
		logger.printInfo("Succesfully Posted All Messages [Count=" + listCloudMessages.size() + "]", name);
		
		return transformFromPutRecordsResultEntryToCloudResponses(putRecordResult, listCloudMessages);
	}

	@Override
	public void setLogger(CloudLogger logger) {	this.logger = logger;	}

	@Override
	public String getCloudSRN() { return kinesisARN; }
	
	/**---------------------------Transform method from {@link CloudMessage} to {@link PutRecordsRequestEntry} **/
	private List<PutRecordsRequestEntry> transformFromCloudMessageToPutRecordsRequestEntry(List<CloudMessage> listCloudMessages) {
		List<PutRecordsRequestEntry> putRequestRecords = new ArrayList<PutRecordsRequestEntry>();
		for (CloudMessage entry: listCloudMessages) {
			PutRecordsRequestEntry nextEntry = new PutRecordsRequestEntry();
			nextEntry.setData(ByteBuffer.wrap(CloudServiceUtil.serialize(entry.getBody())));
			nextEntry.setPartitionKey(entry.getId());
			putRequestRecords.add(nextEntry);	
		}
		return putRequestRecords;
	}
	/**---------------------------Transform method from {@link PutRecordsRequestEntry} to {@link CloudRe} **/
	private CloudResponses transformFromPutRecordsResultEntryToCloudResponses(PutRecordsResult putRecordResult, List<CloudMessage> listCloudMessages) {
		CloudResponses responses = new CloudResponses();
		int passedCount = 0; int failedCount = 0;
		List<PutRecordsResultEntry> putRecordResults = putRecordResult.getRecords();
		for(int i=0; i < putRecordResults.size(); i++) {
			PutRecordsResultEntry entry = putRecordResults.get(i);
			CloudResponse response = CloudResponse.buildCloudResponse(new CloudRequest(listCloudMessages.get(i)))
					.withProcessedStatus(true)
					.withNativeObject(entry);
			
			String sequenceNumber = entry.getSequenceNumber();
			if(sequenceNumber == null || sequenceNumber.trim().length() == 0) {//Failed Record
				++failedCount;
				response = response.withProcessedStatus(false)
						.withMessage(entry.getErrorMessage())
						.withCode(entry.getErrorCode());
			} else { //Passed Record
				++passedCount;
				response = response.withKeyValue(CloudMessage.Key.SHARD_ID.name(), entry.getShardId())
				.withKeyValue(CloudMessage.Key.SEQUENCE_NUMBER.name(), entry.getSequenceNumber());
			}
			responses.addResponse(response);
		}
		
		logger.printInfo("[Passed Count=" + passedCount + ", Failed Count=" + failedCount  + "]", name);
		return responses;
	}
	
	/**---------------------------{@link CloudResponse} builder methods**/
	private CloudResponse buildCloudResponse(CloudMessage message, Object result) {
		CloudResponse response = new CloudResponse()
				.withCloudRequest(message.wrapIntoCloudRequest())
				.withNativeObject(result);
		
		if (result instanceof PutRecordResult) {
			PutRecordResult localResult = (PutRecordResult)result;
			response = response.withProcessedStatus(true)
					.withId(localResult.getSdkResponseMetadata().getRequestId())
					.withKeyValue(CloudMessage.Key.SHARD_ID.name(), localResult.getShardId())
					.withKeyValue(CloudMessage.Key.SEQUENCE_NUMBER.name(), localResult.getSequenceNumber());
		} else if (result instanceof Exception) {
			Exception localResult = (Exception)result;
			response = response.withProcessedStatus(false)
					.withMessage(localResult.getMessage());
		} 
		
		return response;
	}
	
	private void initialize(Properties props) {
		kinesisARN = props.getProperty(SERVICE_ARNS.getText());
		if (kinesisARN == null) throw new CloudServiceException(serviceExceptionOperation, "Could not find Kinesis Data Service ARN In The Property Argument[" + 
				SERVICE_ARNS.getText() + "]");
		
		kinesisClient = AmazonKinesisClientBuilder.standard().build();
	}
}
