/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.request.accelerator;
import java.util.Properties;

import a3.service.A3Service;

/**
 * @author Brijesh Sharma
 * This interface defines single createCloudService method, which shall be implemented by CloudServiceFactor implementation class.
 *
 */
public interface A3ServiceFactory{
	
	/***/
	public A3Service  createCloudService(Properties props); 
	
	public boolean  doSupportCloudService(String serviceName); 
}
