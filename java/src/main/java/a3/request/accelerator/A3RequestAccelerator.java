/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.request.accelerator;

import static a3.request.accelerator.A3RequestAccelerator.Config.*;
import static a3.service.A3Service.StatusCode.*;

import java.util.*;

import a3.model.request.A3Request;
import a3.model.request.A3Requests;
import a3.model.request.A3Response;
import a3.model.request.A3Responses;
import a3.service.A3Logger;
import a3.service.A3Service;
import a3.service.A3ServiceException;
import a3.service.A3ServiceUtil;

/**
 * @author <b>Brijesh Sharma</b><br>
 * The cloud accelerator decouples cloud request processing stages as below. <br>
 * 1. {@link A3RequestInterceptor} intercepts cloud request and forward it to {@link A3RequestTransformer}.<br>
 * 2. {@link CloudRequestTransfomer} transforms inbound cloud request format to {@link A3Requests} and forward it {@link A3RequestProcessor}<br>
 * 3. {@link A3RequestProcessor} processes {@link CloudRequets}, build {@link A3Responses} and route it to {@link A3RequestTransformer}<br>
 * 4. {@link CloudRequestTransfomer} transforms {@link A3Responses} to outbound format and route it to {@link A3RequestInterceptor}<br>
 * 5.  {@link A3RequestInterceptor} returns response back to caller<br>
 * In addition, cloud accelerator provides below capabilities to {@link A3RequestProcessor}<br>
 * 3.1 Allow configuring multiple {@link A3ServiceFactory}, which can be used to create {@link A3Service}<br>
 * 3.2 Allow configuring multiple {@link A3Service} required by {@link A3RequestProcessor}<br>
 * 3.3 Using configured {@link A3ServiceFactory}, cloud accelerator creates {@link A3Service} and inject them into {@link A3RequestProcessor}<br>
 * ----SAMPLE CONFIGURATION USING AWS CLOUDFORMATION TEMPLATE------<br>
 *   DataProducer:<br>
 *   &nbsp;&nbsp;Type: 'AWS::Lambda::Function'<br>
 *   &nbsp;&nbsp;Properties : <br>
 *       &nbsp;&nbsp;&nbsp;&nbsp;Code:<br>
 *           &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;S3Bucket : Bucket-Name<br>
 *           &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;S3Key: file-containing-cloudRequestProcessorclass.jar<br>
 *       &nbsp;&nbsp;&nbsp;&nbsp;FunctionName : Function-Name<br>
 *       &nbsp;&nbsp;&nbsp;&nbsp;Handler : {@link AWSCloudEventInterceptor}::handleS3Request<br>
 *       &nbsp;&nbsp;&nbsp;&nbsp;Runtime : java8<br>
 *       &nbsp;&nbsp;&nbsp;&nbsp;MemorySize: 500<br>
 *       &nbsp;&nbsp;&nbsp;&nbsp;Timeout: 30<br>
 *       &nbsp;&nbsp;&nbsp;&nbsp;Layers: <br>
 *       &nbsp;&nbsp;&nbsp;&nbsp; - Cloud-Lib-Accelerator.jar<br>
 *       &nbsp;&nbsp;&nbsp;&nbsp;Role : LambdaRoleArn<br>
 *       &nbsp;&nbsp;&nbsp;&nbsp;Environment:<br>
 *           &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Variables:<br>
 *               <b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{@link Config#PROCESSORS}:</b> fully qualified {@link A3RequestProcessor} implementation class name<br>
 *               <b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{@link Config#PROCESSORS}:</b> fully qualified {@link A3RequestProcessor} implementation class name<br>
 *               <b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{@link Config#TRANSFORMER}:</b> fully qualified {@link A3RequestTransformer} implementation class name<br>
 *               <b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{@link Config#SERVICE_TYPES}:</b> !Join [ "~~", [ SQS, DYNAMODB] ]<br>
 *               <b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{@link Config#SERVICE_ARNS}:</b> !Join [ "~~", [ SQSArn, DYNAMODBArn ]]<br>
 *               <b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;CloudRequestProcessor_Property1_Key:</b> CloudRequestProcessor_Property1_Value<br>
 *               <b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;CloudRequestProcessor_Property2_Key:</b> CloudRequestProcessor_Property2_Value<br>
 *
 */
public class A3RequestAccelerator {
	
	public enum Config {
		SERVICE_TYPES("SERVICE_TYPES"), 
		SERVICE_ARNS("SERVICE_ARNS"), 
		PROCESSORS("PROCESSORS"),
		TRANSFORMER("TRANSFORMER"),
		CLOUD_FACTORIES("CLOUD_FACTORIES"),
		DELIMITER("~~");
		private String text;

		private Config(String text){ this.text = text; }
		public String getText() { return text; }
		public void setText(String text) { this.text = text; }
	};
	

	String cloudFactoryClassName = A3ServiceFactory.class.getSimpleName();
	String requestProcessorClassName = A3RequestProcessor.class.getSimpleName();
	String transformerClassName = A3RequestTransformer.class.getSimpleName();
	
	private Properties props = null; // Property reference to be passed by caller
	private boolean isInitialized = false;
	
	private A3Logger logger = null;
	private String name = getClass().getSimpleName();
	private String cloudProcessorNames = null, factoryList = null;
	private A3RequestTransformer transformer = null;
	
	private A3Service cloudServices[] = null; // Class will create each requested Cloud service and inject it into CloudRequestProcessor
	private A3ServiceFactory [] cloudFactories = null;
	private A3RequestProcessor processors[] = null; //Class will load and instantiate each requested CloudRequestProcessor class in the property reference
	
	/**------------------------------------------STARTED - Constructor and Initialization-----------------------------------*/
	public A3RequestAccelerator(Properties props, A3Logger logger) {this.props = props; this.logger = logger;}
	
	public void initialize() {
		loadCloudServiceFactory();
		loadTransformer();
		loadCloudRequestProcessors();
		
		injectMandatoryServices();
		injectCloudServices();
		isInitialized = true;
	}
	
	public boolean isInitialized() { return isInitialized;}
	
	/**------------------------------------------STARTED - Method Supporting Dynamic Loading-----------------------------------*/
	private void loadCloudServiceFactory() {
		factoryList = A3ServiceUtil.getPropertyIgnoreCase(props, Config.CLOUD_FACTORIES.getText());
		printInfo("Loading " + cloudFactoryClassName + " [ " + factoryList + "]. Key Used For Loading " + cloudFactoryClassName + " Is [" + 
				Config.CLOUD_FACTORIES.getText() + "]" );
		if (factoryList == null) {
			logger.printWarn("No Cloud Service Will Be Created By " + name + " Because Property name " + 
					Config.CLOUD_FACTORIES.getText() + " NOT Found In The Caller Configuration", name);
			return;
		}
		String[] factoryNames = factoryList.split(DELIMITER.getText());
		cloudFactories = new A3ServiceFactory[factoryNames.length];
		for(int i=0; i < factoryNames.length; i++) {
			cloudFactories[i] = A3ServiceUtil.loadClassAndInstantiate(factoryNames[i], A3ServiceFactory.class);
			printInfo("Succesfully Loaded " +   cloudFactoryClassName + "[" + factoryNames[i] + "]");
		}
		printInfo("Succesfully Loaded All " + cloudFactoryClassName + " [" + factoryList + "].");	
	}
	private void loadTransformer() {
		String transformerName = A3ServiceUtil.getPropertyIgnoreCase(props, Config.TRANSFORMER.getText());
		printInfo("Loading " + transformerClassName  +  " [ " + transformerName + "]. Key Used For Loading " + transformerClassName  +  " is " + 
				Config.TRANSFORMER.getText() );
		
		if (transformerName == null)
			throw new A3ServiceException("LoadTransformer", "Property name " + 
					Config.TRANSFORMER.getText() + " NOT Found In The Caller Configuration");
		
		transformer =  A3ServiceUtil.loadClassAndInstantiate(transformerName, A3RequestTransformer.class);
		transformer.setLogger(logger);
		printInfo("Succesfully Loaded " + transformerClassName + " [" + transformerName + "]");
	}
	
	private void loadCloudRequestProcessors() {
		cloudProcessorNames = A3ServiceUtil.getPropertyIgnoreCase(props, Config.PROCESSORS.getText());
		printInfo("Loading " + requestProcessorClassName  + " [ " + cloudProcessorNames + "]. Key Used For Loading " + requestProcessorClassName + " Is " + 
				Config.PROCESSORS.getText() );
		if (cloudProcessorNames == null) 
			throw new A3ServiceException("LoadRequestProcessor", "Property name " + 
					Config.PROCESSORS.getText() + " NOT Found In The Caller Configuration");
		
		String [] reqProcessorClassNames = cloudProcessorNames.split(DELIMITER.getText());
		processors = new A3RequestProcessor[reqProcessorClassNames.length];
		for(int i=0; i < reqProcessorClassNames.length; i++) {
			processors[i] = A3ServiceUtil.loadClassAndInstantiate(reqProcessorClassNames[i], A3RequestProcessor.class);
			printInfo("Succesfully Loaded " + requestProcessorClassName + " [" + reqProcessorClassNames[i] + "]");
		}
		printInfo("Succesfully Loaded All " + requestProcessorClassName + " [" + cloudProcessorNames + "].");			
	}
	/**------------------------------------------COMPLETED - Method Supporting Dynamic Loading-----------------------------------*/

	/**------------------------------------------STARTED - Method Supporting Dependency Injection-----------------------------------*/
	private void injectMandatoryServices( ) {
		printInfo("Initiating Logger and Properties Services Injection In All " + requestProcessorClassName + "[" + cloudProcessorNames  + "]");
		for (A3RequestProcessor processor: processors) {
			processor.setLogger(logger);//Set logger, property and Request references
			processor.setProperties(props);
			printInfo("Succesfully Injected Logger And Properties in " + requestProcessorClassName + "[" + processor.getName() + "]");
		}
		printInfo("Succesfully Injected Logger And Properties in All " + requestProcessorClassName + " [" + cloudProcessorNames  + "]");
	}
	private void injectCloudServices( ) {
		printInfo("Initiating Cloud Service Injection for All " + requestProcessorClassName + " [" + cloudProcessorNames  + "]");
		String cloudServicesRequested = A3ServiceUtil.getPropertyIgnoreCase(props, Config.SERVICE_TYPES.getText());
		String cloudServicesRequestedARN = A3ServiceUtil.getPropertyIgnoreCase(props, Config.SERVICE_ARNS.getText());
		printInfo("Cloud Services Requested are [" + cloudServicesRequested + "]. Requested Cloud Services Requested Resource Name are: [" + cloudServicesRequestedARN + "]");
		
		if (cloudServicesRequested == null || cloudServicesRequestedARN == null) {
			logger.printWarn(name + ": Returning From InjectCloudServices Without Injecting Any Cloud Service Because Either Of The  Property Names " + 
					Config.SERVICE_TYPES.getText() + ", OR Property Name " + 
					Config.SERVICE_ARNS.getText() + " NOT Found", name);
			return;
		}
		
		String [] cloudServiceNames = cloudServicesRequested.split(DELIMITER.getText());
		String [] cloudServicesARNs = cloudServicesRequestedARN.split(DELIMITER.getText());
		if (cloudServiceNames.length != cloudServicesARNs.length) {
			logger.printWarn(name + ": Returning From InjectCloudServices Without Injecting Any Cloud Service Because Number of Requested Cloud Services=" + 
					cloudServiceNames.length + " does not match with number of Cloud Service ARN=" + cloudServicesARNs.length, name);
			return;
		}
		
		cloudServices = new A3Service[cloudServiceNames.length];
		int i = 0;
		try { 
			outer: for(i = 0; i < cloudServiceNames.length; i++) {
				//Reset Requested Service Name and ARN as per the next service
				props.setProperty(Config.SERVICE_TYPES.getText(), cloudServiceNames[i]);
				props.setProperty(Config.SERVICE_ARNS.getText(), cloudServicesARNs[i]);
				
				for(A3ServiceFactory factory: cloudFactories) {
					if (!factory.doSupportCloudService(cloudServiceNames[i])) continue;
					
					printInfo("Creating Cloud Service [" + cloudServiceNames[i] + "] With ARN [" + cloudServicesARNs[i]  +  
							"] Using CloudFactory [" + factory.getClass().getSimpleName() + "]");
					cloudServices[i] = factory.createCloudService(props);
					cloudServices[i].setLogger(logger);
					printInfo("Requested Cloud Service [" + cloudServiceNames[i] + "] With ARN [" + cloudServicesARNs[i]  +  "] Created Succesfully");
					continue outer;
				}
				
				throw new A3ServiceException("CreateCloudServie", "None Of The Configured " + cloudFactoryClassName + 
						" [" + factoryList + "] Support Creating Cloud Service [" + cloudServiceNames[i] + "]");
				
			}
			printInfo("All Requested Cloud Services Created Succesfully");
			
		}catch(A3ServiceException exception) {
			logger.printErr("Inject Request Processor Failed, Aboarting Cloud Services Injection. Error Creating Cloud Service " + cloudServiceNames[i]
					+ ". Exception " + exception.getMessage(), name);
			throw exception;
		}
		
		//Reset Requested Property name and ARN to original
		props.setProperty(SERVICE_TYPES.getText(), cloudServicesRequested);
		props.setProperty(SERVICE_ARNS.getText(), cloudServicesRequestedARN);
		
		injectCloudServices(processors, cloudServices);
		printInfo("Succesfully Injected All Cloud Services");
	}

	private void injectCloudServices(A3RequestProcessor [] processors, A3Service[] cloudServices) {
		for (A3RequestProcessor processor: processors) {
			processor.setCloudServices(cloudServices);
			printInfo("Succesfully Injected All Cloud Services in Request Processor " + processor.getName());			
		}
	}
	/**------------------------------------------COMPLETED - Method Supporting Dependency Injection-----------------------------------*/
	
	public Object executeCloudRequestProcessors( Object objRequest) {
		List<A3Response> listResponse = new ArrayList<A3Response>();
		A3Request cloudRequest = null;
		for ( A3RequestProcessor processor: processors) {
			logger.printBlankLine();
			printInfo(" STARTED: CloudRequestProcessor [" +  processor.getName() + "]-------------------------------------");
			cloudRequest = transformer.transform(objRequest);
			processor.setPayload(cloudRequest);
			A3Response handlerResponse = null;
			try {
				handlerResponse = processor.execute();
				printInfo(" COMPLETED: CloudRequestProcessor [" +  processor.getName() + "]-------------------------------------");
				logger.printBlankLine();
			} catch (Exception exception) {
				logger.printErr(" COMPLETED With Error: CloudRequestProcessor [" +  processor.getName() + 
						"]. EXCEPTION: " + exception.getMessage(), name);
				logger.printBlankLine();
				handlerResponse = A3Response.buildErrorCloudResponse(cloudRequest, INTERNAL_ERROR.code(), 
						INTERNAL_ERROR.name(), exception.getMessage());
				exception.printStackTrace();
			}
			listResponse.add(handlerResponse);
		}
		try { 
			if (listResponse.size() == 1) return transformer.transform(listResponse.get(0));
			return transformer.transformResponse(listResponse); 
		} catch(Exception exception) {
			String errMsg = "Error Transforming Response. EXCEPTION:" + exception.getMessage();
			logger.printErr(errMsg, name);
			exception.printStackTrace();
			return  A3Response.buildErrorCloudResponse(null, INTERNAL_ERROR.code(), 
					INTERNAL_ERROR.name(), errMsg);
		}
	}
	
	private void printInfo(String message) {logger.printInfo( message, name);}
}
