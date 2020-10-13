/**
 * 
 */
package a3.service.aws;
import static cloud.service.CloudService.Property.*;

import java.util.Properties;

import cloud.request.accelerator.CloudServiceFactory;
import cloud.service.CloudService;
import cloud.service.CloudServiceException;
import cloud.service.CloudServiceUtil;

/**
 * @author <br>Brijesh Sharma</b><br>
 * The class is an implementation of {@link CloudServiceFactory} responsible for creating various CloudService for AWS Cloud provider. 
 */
public class AWSServiceFactory implements CloudServiceFactory {
	
	public enum Service{STEP_FUNCTIONS, DYNAMODB, SQS, MEMCACHED, S3, SNS, KINESIS_DATA_STREAM, API_GATEWAY, SECRET_MANAGER, SES };
	
	private String strCloudProvider = "AWS"; 
	private String name = getClass().getSimpleName();
	
	/** ---------------------------------------Constructor-----------------------------------------*/
	public AWSServiceFactory() { }
	
	/** ---------------------------------------{@link CloudServiceFactory} implementation-----------------------------------------*/
	@Override
	public boolean doSupportCloudService(String serviceName) {
		return CloudServiceUtil.doSupportCloudService(serviceName, Service.class);
	}
	
	@Override
	public CloudService createCloudService(Properties props) throws CloudServiceException {
		
		String cloudServiceOperation = CLOUD_SERVICE_OPERATION.getText().replaceFirst(CLOUD_PROVIDER_SUBSITUTOR.getText(), strCloudProvider);
		Service requestedService = CloudServiceUtil.getEnumTypeIgnoreCase_ThrowErrorIfNotExists(Service.class, props, this, strCloudProvider);
		cloudServiceOperation = cloudServiceOperation.replaceFirst(SERVICE_NAME_SUBSITUTOR.getText(), requestedService.name());

		switch (requestedService) {
			case STEP_FUNCTIONS: return new AWSStateMachineService(props);
			case DYNAMODB: return new AWSDynamoDBService(props);
			case SQS: return new AWSSQSService(props);
			case MEMCACHED: return new AWSMemcachedService(props);
			case S3: return new AWSS3Service(props);
			case SNS: return new AWSSNSService(props);
			case KINESIS_DATA_STREAM: return new AWSKinesisDataStreamService(props);
			case API_GATEWAY: return new AWSAPIGateway(props);
			case SECRET_MANAGER: return new AWSSecretManagerService(props);
			case SES: return new AWSEmailService(props);
				
			default: throw new CloudServiceException(cloudServiceOperation, "Requested Service " + requestedService + " is currently NOT supported by [" + 
					name + "]");
		}
	}
}
