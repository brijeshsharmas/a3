/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.request.accelerator;

import java.util.Properties;

import a3.model.request.A3Request;
import a3.model.request.A3Response;
import a3.service.A3Logger;
import a3.service.A3Service;

/**
 * @author <b>Brijesh Sharma:</b><br> 
 * The {@link A3RequestProcessor} is a contract defined as part of {@link A3RequestAccelerator} accelerator, which developer can implement to 
 * decouple business logic from cloud infrastructure and services. As an alternate, developer can choose to extend a default base implementation 
 * class {@link A3RequestProcessorBaseImpl }, which provides default implementation of all {{@link A3RequestProcessor}} methods.  
 * <p>
 * The developer can request CloudService injection in the {@link A3RequestProcessor} for accessing cloud infrastructure and services. 
 * The {@link A3RequestAccelerator} inject requested Cloud Services in {@link #setCloudServices(A3Service[] cloudServices)} method. 
 * The array size and order of Cloud Services in the array remain same as requested by caller.
 * </p>
 * <p>
 * The accelerator {@link A3RequestAccelerator} also take care of its payload by transforming incoming request in any format to {@link A3Request} format through
 * configured {@link A3RequestTransformer} and vice-a-versa transforming {@link A3Response} back to desired format.
 * </p>
 * The developer must override {@link #execute()} method for custom implementation 
 */
public interface A3RequestProcessor {

	/**
	 * {@link A3RequestProcessor}  must implement this method with desired business logic.
	 */
	public A3Response execute();
	
	
	/**
	 * Array cloudServices size and order of AWSService is same as requested by caller through AWS Core Services Serverless Framework.
	 * @param cloudServices
	 */
	public void setCloudServices(A3Service [] cloudServices);
	
	
	/**
	 * 
	 * @param requests
	 */
	public void setPayload(A3Request request);
	
	
	/**
	 * 
	 * @param logger
	 */
	public void setLogger (A3Logger logger);
	
	
	/**
	 * AWS Core Serverless Framework will inject Properties objected populated with user defined properties
	 * @param props
	 */
	public void setProperties (Properties props);
	
	/**
	 * Implementation class use this method to provide name to their respective BusinessHandler implementation
	 * @param name
	 */
	public void setName(String name);
	
	public String getName();
		
	
}
