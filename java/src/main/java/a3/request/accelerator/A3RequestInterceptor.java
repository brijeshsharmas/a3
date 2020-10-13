/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.request.accelerator;

import static a3.service.A3Service.StatusCode.*;

import java.util.Properties;

import a3.model.request.A3Requests;
import a3.model.request.A3Response;
import a3.model.request.A3Responses;
import a3.request.accelerator.A3RequestAccelerator;
import a3.service.A3Logger;
import a3.service.A3Service;
import a3.service.A3ServiceException;
/**
 * 
 * @author <b>Brijesh Sharma</b><br>
 * This class is the handler to {@link A3RequestProcessor} accelerator. It intercepts all incoming  {@link A3RequestProcessor} accelerator
 * requests. The handler require initialization before it can intercepts requests, as mentioned below. For understanding how to use the handler, 
 * please see {@link AWSCloudEventInterceptor}, which route all inbound AWS Lambda events to the handler<br><br>
 * 
 * How does it work ---><br> 
 * 1. Initialization {@link #initialize(A3ServiceFactory, A3Logger, Properties)}<br>
 * 		&nbsp;&nbsp;&nbsp;&nbsp;1.1 Instantiate {@link A3RequestAccelerator} using {@link A3Logger} and Properties input<br>
 * 		&nbsp;&nbsp;&nbsp;&nbsp;1.2 Inject CloudSpecific {@link A3ServiceFactory} instance to {@link A3RequestAccelerator}, which internally uses it 
 * to create cloud specific cloud services<br>
 * 		&nbsp;&nbsp;&nbsp;&nbsp;1.3 Load implementation of all the {@link A3RequestProcessor} interface configured as dependencies in the properties.<br>
 * 		&nbsp;&nbsp;&nbsp;&nbsp;1.4 Create all required {@link A3Service} as configured in the Properties and inject them in all the {@link A3RequestProcessor}<br>
 * 2. Process Message {@link #processRequest(Object, Object)}<br>
 * 		&nbsp;&nbsp;&nbsp;&nbsp;2.1 Using {@link A3RequestTransformer} object configured by caller, transform cloud specific event source object to 
 * 			{@link A3Requests} object.<br>
 * 		&nbsp;&nbsp;&nbsp;&nbsp;2.2 Inject {@link A3Requests} as payload to each loaded (during Initialization) {@link A3RequestProcessor}.<br>
 * 		&nbsp;&nbsp;&nbsp;&nbsp;2.3 Invoke {@link A3RequestProcessor#execute()} function on all the implementation of {@link A3RequestProcessor}, which return 
 * 			response in  {@link A3Responses}  object <br>
 * 		&nbsp;&nbsp;&nbsp;&nbsp;2.4 Using configured {@link A3RequestTransformer}, transform {@link A3Responses} object back to desired format.<br>
 */
public class A3RequestInterceptor  {
	
	private A3RequestAccelerator cloudAccelerator; 
	private A3Logger logger = null;
	private Properties props = null;
	private String name = getClass().getSimpleName();
	private String initRecordedErrorMessage = null;

	public void initialize(A3Logger logger, Properties properties) {	
		if (cloudAccelerator == null) { 
			try {
				this.logger = logger;
				this.props = properties;
				cloudAccelerator = new A3RequestAccelerator(props, logger);
				cloudAccelerator.initialize();
			}catch(Exception exception ) {
				exception.printStackTrace();
				initRecordedErrorMessage = "Exception Initializing " + name + ". EXCEPTION= " +  
						exception.getMessage() + ". Please check " + name + " step-by-step initialization logs";
			}
		}
	}
	
	public Object processRequest(Object eventObject, Object context) {
		
		if (!cloudAccelerator.isInitialized())
			return new A3Response().withProcessedStatus(false).withCode(INTERNAL_ERROR.code()).withShortMessage(INTERNAL_ERROR.name())
					.withMessage(initRecordedErrorMessage).toJsonString();
		
		try { return cloudAccelerator.executeCloudRequestProcessors(eventObject); 			
		}catch(Exception e) {
			logger.printErr("Exception in " + name + ". Exception " + e, name);
			e.printStackTrace();
			throw new A3ServiceException(name,"Error Processing Cloud Message Event. Exception " + e.getMessage());	
		}	
	}
	
	public String getName() { return name; }
}
