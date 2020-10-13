/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.service.irs;

import static a3.service.A3Service.Property.CLOUD_PROVIDER_SUBSITUTOR;
import static a3.service.A3Service.Property.CLOUD_SERVICE_OPERATION;
import static a3.service.A3Service.Property.SERVICE_NAME_SUBSITUTOR;

import java.util.Properties;

import a3.request.accelerator.A3ServiceFactory;
import a3.service.A3Service;
import a3.service.A3ServiceException;
import a3.service.A3ServiceUtil;
import a3.service.irs.IrsTemplateJsonService;
import a3.service.irs.email.IrsEmailServiceImpl;

public class IrsCloudFactory implements A3ServiceFactory{
	
	public enum Service{TEMPLATE, EMAIL};
	
	private String strCloudProvider = "Twilio"; 
	private String name = getClass().getSimpleName();
	
	/** ---------------------------------------{@link A3ServiceFactory} implementation-----------------------------------------*/
	@Override
	public boolean doSupportCloudService(String serviceName) {
		return A3ServiceUtil.doSupportCloudService(serviceName, Service.class);
	}
	
	@Override
	public A3Service createCloudService(Properties props) {
		
		String cloudServiceOperation = CLOUD_SERVICE_OPERATION.getText().replaceFirst(CLOUD_PROVIDER_SUBSITUTOR.getText(), strCloudProvider);
		Service requestedService = A3ServiceUtil.getEnumTypeIgnoreCase_ThrowErrorIfNotExists(Service.class, props, this, strCloudProvider);
		cloudServiceOperation = cloudServiceOperation.replaceFirst(SERVICE_NAME_SUBSITUTOR.getText(), requestedService.name());

		switch (requestedService) {
			case EMAIL: return new IrsEmailServiceImpl(props);
			case TEMPLATE: return new IrsTemplateJsonService(props);
				
			default: throw new A3ServiceException(cloudServiceOperation, "Requested Service " + requestedService + " is currently NOT supported by [" + 
					name + "]");
		}
	}
}
