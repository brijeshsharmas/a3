/**
 * 
 */
package a3.service.aws;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.BatchResultErrorEntry;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageBatchResult;
import com.amazonaws.services.sqs.model.SendMessageBatchResultEntry;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;

import cloud.model.message.CloudMessage;
import cloud.model.request.CloudResponse;
import cloud.model.request.CloudResponses;
import cloud.service.CloudLogger;
import cloud.service.CloudMessageService;
import cloud.service.CloudServiceException;

import static a3.service.aws.AWSServiceFactory.Service.*;
import static cloud.request.accelerator.CloudRequestAccelerator.Config.*;
import static cloud.service.CloudService.Property.*;

import java.util.*;

/**
 * @author <b>Brijesh Sharma</b><br>
 * The class is {@link CloudMessageService} implementation for AWS Cloud Service named Simple Queue Service (SQS)
 *
 */
public class AWSSQSService implements CloudMessageService {

	private AmazonSQS sqsClient = null;
	private String name = getClass().getSimpleName();
	private String sqsQueueARN = null;
	private final int BATCH_SIZE = 10;
	private CloudLogger logger = null;
	private String serviceExceptionOperation = CLOUD_SERVICE_OPERATION.getText().
			replaceFirst(CLOUD_PROVIDER_SUBSITUTOR.getText(), "AWS").
			replaceFirst(SERVICE_NAME_SUBSITUTOR.getText(), SQS.name());

	
	/**----------------------------------------------------Constructor-------------------------------------------------*/
	public AWSSQSService(Properties props) { initialize(props);}
 
	/**----------------------------------------------------{@link CloudMessageService} Implementation-------------------------------------------------*/
	@Override
	public CloudResponse publishMessage(CloudMessage cloudMessage) {
		
		SendMessageRequest sendMessageStandardQueue = new SendMessageRequest().withQueueUrl(sqsQueueARN).withMessageBody(cloudMessage.getBody());	
		try {
			SendMessageResult result = sqsClient.sendMessage(sendMessageStandardQueue);
			logger.printInfo("Succesfully Posted Message [Id=" + cloudMessage.getId() + ", Body=" + cloudMessage.getBody() + "]", name);
			return buildCloudResponseForSuccess(cloudMessage, result);
			
		}catch (Exception e) {
			e.printStackTrace();
			return buildCloudResponseForError(cloudMessage, e);
		}
	}
	
	@Override
	public CloudResponses publishMessagesInBatch(List<CloudMessage> listCloudMessages) {
		
		CloudResponses cloudResponses = new CloudResponses();
		
		List <SendMessageBatchRequestEntry> listBatchEntry = null;
		for (int i = 0; i < listCloudMessages.size(); i++) {	
			CloudMessage nextMessage = listCloudMessages.get(i);
			
			if (i % BATCH_SIZE == 0) {//Send in batches
				if (listBatchEntry == null) {
					listBatchEntry = new ArrayList<SendMessageBatchRequestEntry>();
					SendMessageBatchRequestEntry batchEntry = new SendMessageBatchRequestEntry()
							.withId(nextMessage.getId())
							.withMessageBody(nextMessage.getBody());
					listBatchEntry.add(batchEntry);
					continue;
				}
				
				SendMessageBatchRequest batchRequest = new SendMessageBatchRequest().withQueueUrl(sqsQueueARN).withEntries(listBatchEntry);
				SendMessageBatchResult result =  sqsClient.sendMessageBatch(batchRequest);
				fillResponse(result, cloudResponses, listCloudMessages);
				listBatchEntry = new ArrayList<SendMessageBatchRequestEntry>();
			}
			
			SendMessageBatchRequestEntry batchEntry = new SendMessageBatchRequestEntry()
					.withId(nextMessage.getId())
					.withMessageBody(nextMessage.getBody());
			listBatchEntry.add(batchEntry);
		}
		
		if (listBatchEntry !=null && listBatchEntry.size() > 0) {
			SendMessageBatchRequest batchRequest = new SendMessageBatchRequest().withQueueUrl(sqsQueueARN).withEntries(listBatchEntry);
			SendMessageBatchResult result =  sqsClient.sendMessageBatch(batchRequest);
			fillResponse(result, cloudResponses, listCloudMessages);
		}
		
		logger.printInfo("Succesfully Posted All Messages [Count=" + listCloudMessages.size() + "] in Batch Size Of=" + BATCH_SIZE, name);
		return cloudResponses;
	}

	@Override
	public void setLogger(CloudLogger logger) { this.logger = logger; }

	@Override
	public String getCloudSRN() { return sqsQueueARN; }
	
	/**----------------------------------------------------Helper Methods-------------------------------------*/
	private void fillResponse(SendMessageBatchResult result, CloudResponses responesToFill, List<CloudMessage> listCloudMessages) {
		
		for (BatchResultErrorEntry batchResultErrorEntry: result.getFailed()) { 
			CloudMessage originalCloudMessage = findOrCreateIfMissingCloudMessageInList(listCloudMessages, batchResultErrorEntry.getId());
			CloudResponse responseEntry = buildCloudResponseForError(originalCloudMessage, batchResultErrorEntry);
			responesToFill.addResponse(responseEntry);
		}
		
		for (SendMessageBatchResultEntry batchResultSuccessEntry: result.getSuccessful()) {
			CloudMessage originalCloudMessage = findOrCreateIfMissingCloudMessageInList(listCloudMessages, batchResultSuccessEntry.getId());
			CloudResponse responseEntry = buildCloudResponseForSuccess(originalCloudMessage, batchResultSuccessEntry);
			responesToFill.addResponse(responseEntry);
		}
	}

	/**---------------------------{@link CloudResponse} builder methods**/
	private CloudResponse buildCloudResponseForSuccess(CloudMessage message, Object result) {
		CloudResponse response = new CloudResponse()
				.withCloudRequest(message.wrapIntoCloudRequest())
				.withObject(message)
				.withProcessedStatus(true)
				.withNativeObject(result);
		String messageId = null, md5Body = null;
		if (result instanceof SendMessageResult) {
			messageId = ((SendMessageResult)result).getMessageId();
			md5Body = ((SendMessageResult)result).getMD5OfMessageBody();
		} else if (result instanceof SendMessageBatchResultEntry) {
			messageId = ((SendMessageBatchResultEntry)result).getMessageId();
			md5Body = ((SendMessageBatchResultEntry)result).getMD5OfMessageBody();
		}
		
		response = response.withId(messageId).withKeyValue(CloudMessage.Key.MD5_HASH.name(), md5Body);
		return response;
	}
	private CloudResponse buildCloudResponseForError(CloudMessage cloudMessage, Object errorObject) {
		CloudResponse response = new CloudResponse()
				.withCloudRequest(cloudMessage.wrapIntoCloudRequest())
				.withObject(cloudMessage)
				.withProcessedStatus(false);
		String message = null, code = null;
		if (errorObject instanceof BatchResultErrorEntry) {
			response = response.withNativeObject(errorObject);
			message = ((BatchResultErrorEntry)errorObject).getMessage();
			code = ((BatchResultErrorEntry)errorObject).getCode();
		} else if (errorObject instanceof Exception) {
			message = ((Exception)errorObject).getMessage();
		}
		
		response = response.withMessage(message).withCode(code);
		return response;	
	}
	
	private void initialize(Properties props) {
		sqsQueueARN = props.getProperty(SERVICE_ARNS.getText());
		if (sqsQueueARN == null) 
			throw new CloudServiceException(serviceExceptionOperation, "Could not find SQS Service ARN In The Property Argument[" + 
					SERVICE_ARNS.getText() + "]");
		
		sqsClient = AmazonSQSClientBuilder.defaultClient();
	}
	
	private CloudMessage findOrCreateIfMissingCloudMessageInList(List<CloudMessage> listCloudMessages, String id) {
		for (CloudMessage message: listCloudMessages) {
			if (message.getId().equalsIgnoreCase(id))
				return message;
		}
		return new CloudMessage(id);
	}
}
