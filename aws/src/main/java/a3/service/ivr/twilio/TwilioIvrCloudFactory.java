package a3.service.ivr.twilio;

import static cloud.service.CloudService.Property.CLOUD_PROVIDER_SUBSITUTOR;
import static cloud.service.CloudService.Property.CLOUD_SERVICE_OPERATION;
import static cloud.service.CloudService.Property.SERVICE_NAME_SUBSITUTOR;

import java.util.Properties;

import cloud.request.accelerator.CloudServiceFactory;
import cloud.service.CloudService;
import cloud.service.CloudServiceException;
import cloud.service.CloudServiceUtil;

public class TwilioIvrCloudFactory implements CloudServiceFactory{
	
	public enum Service{IVR_VOICE};
	
	private String strCloudProvider = "Twilio"; 
	private String name = getClass().getSimpleName();
	
	/** ---------------------------------------{@link CloudServiceFactory} implementation-----------------------------------------*/
	@Override
	public boolean doSupportCloudService(String serviceName) {
		return CloudServiceUtil.doSupportCloudService(serviceName, Service.class);
	}
	
	@Override
	public CloudService createCloudService(Properties props) {
		
		String cloudServiceOperation = CLOUD_SERVICE_OPERATION.getText().replaceFirst(CLOUD_PROVIDER_SUBSITUTOR.getText(), strCloudProvider);
		Service requestedService = CloudServiceUtil.getEnumTypeIgnoreCase_ThrowErrorIfNotExists(Service.class, props, this, strCloudProvider);
		cloudServiceOperation = cloudServiceOperation.replaceFirst(SERVICE_NAME_SUBSITUTOR.getText(), requestedService.name());

		switch (requestedService) {
			case IVR_VOICE: return new TwilioIvrService(props);
				
			default: throw new CloudServiceException(cloudServiceOperation, "Requested Service " + requestedService + " is currently NOT supported by [" + 
					name + "]");
		}
	}
}
