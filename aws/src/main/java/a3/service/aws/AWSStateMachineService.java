/**
 * 
 */
package a3.service.aws;

import com.amazonaws.services.stepfunctions.AWSStepFunctions;
import com.amazonaws.services.stepfunctions.AWSStepFunctionsClientBuilder;
import com.amazonaws.services.stepfunctions.model.StartExecutionRequest;
import com.amazonaws.services.stepfunctions.model.StartExecutionResult;

import cloud.service.CloudLogger;
import cloud.service.CloudOrchestrationService;
import cloud.service.CloudServiceException;
import cloud.service.CloudServiceUtil;

import java.util.Properties;

import static a3.service.aws.AWSServiceFactory.Service.*;
import static cloud.request.accelerator.CloudRequestAccelerator.Config.*;
import static cloud.service.CloudService.Property.*;

/**
 * @author brisharm0
 *
 */
public class AWSStateMachineService implements CloudOrchestrationService {

	private AWSStepFunctions stepFuncClient = null;
	private String strAWSSMARN = null;
	private final int executionNameLength = 15;
	private String serviceExceptionOperation = CLOUD_SERVICE_OPERATION.getText().
			replaceFirst(CLOUD_PROVIDER_SUBSITUTOR.getText(), "AWS").
			replaceFirst(SERVICE_NAME_SUBSITUTOR.getText(), STEP_FUNCTIONS.name());
	/**
	 * 
	 */
	public AWSStateMachineService(Properties props) throws CloudServiceException{
		
		strAWSSMARN = props.getProperty(SERVICE_ARNS.getText());
		if (strAWSSMARN == null) throw new CloudServiceException(serviceExceptionOperation, "Could not find service ARN in the property argument");
		
		stepFuncClient = AWSStepFunctionsClientBuilder.defaultClient();
	}

	@Override
	public String startExecution(String input) {
		
		return startExecutionWithName(input, CloudServiceUtil.generateRandonAlphaNumberic(executionNameLength, 4, "-"));

	}

	@Override
	public String startExecutionWithName(String input, String executionName) {
		StartExecutionRequest request = new StartExecutionRequest().withName(executionName).withStateMachineArn(strAWSSMARN).withInput(input);
		StartExecutionResult result = stepFuncClient.startExecution(request);
		return result.toString();

	}

	@Override
	public void setLogger(CloudLogger logger) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getCloudSRN() {
		// TODO Auto-generated method stub
		return strAWSSMARN;
	}
	
	
}
