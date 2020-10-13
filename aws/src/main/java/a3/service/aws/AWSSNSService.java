/**
 * 
 */
package a3.service.aws;

import java.util.List;
import java.util.Properties;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

import cloud.model.message.CloudMessage;
import cloud.model.request.CloudResponse;
import cloud.model.request.CloudResponses;
import cloud.service.CloudLogger;
import cloud.service.CloudMessageService;
import cloud.service.CloudServiceException;

import static a3.service.aws.AWSServiceFactory.Service.*;
import static cloud.request.accelerator.CloudRequestAccelerator.Config.*;
import static cloud.service.CloudService.Property.*;

/**
 * @author <b>Brijesh Sharma</b><br>
 * The class is {@link CloudMessageService} implementation for AWS Cloud Service named Simple Notification Service (SNS)
 *
 */
public class AWSSNSService implements CloudMessageService {
	
	private String strSNSTopicARN = null;
	AmazonSNS awsSNSClient = null;
	private String name = getClass().getSimpleName();
	private CloudLogger logger = null;
	private String serviceExceptionOperation = CLOUD_SERVICE_OPERATION.getText().
			replaceFirst(CLOUD_PROVIDER_SUBSITUTOR.getText(), "AWS").
			replaceFirst(SERVICE_NAME_SUBSITUTOR.getText(), SNS.name());

	/**----------------------------------------------------Constructor-------------------------------------------------*/
	public AWSSNSService(Properties props) { initialize(props);}
	
	/**----------------------------------------------------{@link CloudMessageService} Implementation-------------------------------------------------*/
	@Override
	public CloudResponse publishMessage(CloudMessage cloudMessage) {
		try {
			PublishRequest request = new PublishRequest(strSNSTopicARN, cloudMessage.getBody(), cloudMessage.getSubject());
			PublishResult result = awsSNSClient.publish(request);
			logger.printInfo( "Succesfully Posted Message [Id=" + cloudMessage.getId() + ", Body=" + cloudMessage.getBody() + "]", name);
			
			return buildCloudResponse(cloudMessage, result);
		} catch(Exception exception) {
			exception.printStackTrace();
			return buildCloudResponse(cloudMessage, exception);
		}
	}

	@Override
	public CloudResponses publishMessagesInBatch(List<CloudMessage> listCloudMessages) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLogger(CloudLogger logger) {this.logger = logger;	}

	@Override
	public String getCloudSRN() {return strSNSTopicARN;}
	
	/**---------------------------{@link CloudResponse} builder methods**/
	private CloudResponse buildCloudResponse(CloudMessage message, Object result) {
		CloudResponse response = new CloudResponse()
				.withCloudRequest(message.wrapIntoCloudRequest())
				.withNativeObject(result);
		
		if (result instanceof PublishResult) {
			PublishResult localResult = (PublishResult)result;
			response = response.withProcessedStatus(true)
					.withId(localResult.getMessageId());
		} else if (result instanceof Exception) {
			Exception localResult = (Exception)result;
			response = response.withProcessedStatus(false)
					.withMessage(localResult.getMessage());
		} 
		
		return response;
	}

	/**----------------------------------------------------Helper Methods-------------------------------------*/
	private void initialize(Properties props) {
		strSNSTopicARN = props.getProperty(SERVICE_ARNS.getText());
		if (strSNSTopicARN == null) throw new CloudServiceException(serviceExceptionOperation, 
				"Could not find SNS Service ARN In The Property Argument[" + 
						SERVICE_ARNS.getText() + "]");

		awsSNSClient = AmazonSNSClientBuilder.defaultClient();
	}
}
