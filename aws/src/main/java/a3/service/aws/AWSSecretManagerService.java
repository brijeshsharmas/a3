/**
 * 
 */
package a3.service.aws;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.DecryptionFailureException;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.InternalServiceErrorException;
import com.amazonaws.services.secretsmanager.model.InvalidParameterException;
import com.amazonaws.services.secretsmanager.model.InvalidRequestException;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cloud.model.request.CloudRequest;
import cloud.model.request.CloudResponse;
import cloud.service.CloudAuthenticationService;
import cloud.service.CloudLogger;
import cloud.service.CloudServiceUtil;

import static cloud.service.CloudService.StatusCode.BAD_REQUEST;
import static cloud.service.CloudService.StatusCode.INTERNAL_ERROR;
import static cloud.service.CloudService.StatusCode.SUCCESS;
import static cloud.service.CloudService.StatusCode.UNAUTHORIZED;
import static cloud.service.CloudService.StatusCode.NOT_FOUND;

import static cloud.service.CloudAuthenticationService.Key.AUTH_TYPE;
import static cloud.service.CloudAuthenticationService.Key.CREDENTIAL_STORE;
import static cloud.service.CloudAuthenticationService.Key.PASSWORD;
import static cloud.service.CloudAuthenticationService.Key.USER_ID;
import static cloud.service.CloudAuthenticationService.AuthType.USER_AND_PASSWORD;
import static cloud.service.CloudAuthenticationService.AuthType.USER_ID_ONLY;

/**
 * @author Brijesh Sharma<br>
 *
 */
public class AWSSecretManagerService implements CloudAuthenticationService {

	private Properties props = null;
	private String name = getClass().getName();
	private AWSSecretsManager secretManagerClient = null;
	private CloudLogger logger = null;
	private List<String> suppressFields = Arrays.asList(CREDENTIAL_STORE.getText(), USER_ID.getText(), PASSWORD.getText(), AUTH_TYPE.getText());
	private Map<String, Map<String, String>> keyValuePair = new HashMap<String, Map<String,String>>();
	
	public AWSSecretManagerService(Properties props) { 	this.props = props; initialize(this.props);}

	/** Method Story Flow:
	 * Step 1: Cache User/Password Against Credential Store If Not Already Cached
	 * Step 2: From The Cache, Get Password Against Cloud Request User Id. If User Id Does Not Contain In The Cache, Reload Cache One More Time
	 * Step 3: Return Password From The Cache
	 */
	@Override
	public CloudResponse getPassword(CloudRequest request) {
		//BEGIN - Validation
		String credentialStore =  request.getValueByKeyIgnoreCase(CREDENTIAL_STORE.getText());
		if(credentialStore == null) return CloudResponse.buildErrorCloudResponse(request, INTERNAL_ERROR.code(), INTERNAL_ERROR.code(), 
				"Internal Configuration Error. Cloud Not Find Credential Store Name In Cloud Request");
		String userid = request.getValueByKeyIgnoreCase(USER_ID.getText());
		if(userid == null) return CloudResponse.buildErrorCloudResponse(request, BAD_REQUEST.code(), BAD_REQUEST.code(), 
				"Cloud Request Does Not Contains User Id");
		//END - Validation
		
		//Cache If Required
		CloudResponse response = null;
		if(!keyValuePair.containsKey(credentialStore) || keyValuePair.get(credentialStore).get(userid) == null) {
			response = cache(request, credentialStore);
			if(!response.processed()) return response;
		}
		
		//Check If Credential Store Cached
		if(!keyValuePair.containsKey(credentialStore))
			return CloudResponse.buildErrorCloudResponse(request, INTERNAL_ERROR.code(), INTERNAL_ERROR.code(), 
					"Internal Configuration Error. Cloud Not Find Credential Store Name In Cloud Request");
		
		//Return Password
		String password = keyValuePair.get(credentialStore).get(userid);
		if(password == null) return buildAndPrintAuthenticationWarning(request, "User Id/Password Does Not Match", UNAUTHORIZED);
		
		return new CloudResponse().withKeyValue(PASSWORD.getText(), password);
	}
	
	/** Method Story Flow:
	 * Step 1: Determine Requested {@link Key#AUTH_TYPE} i.e. either {@link AuthType#USER_ID_ONLY} {@link AuthType#USER_AND_PASSWORD}
	 * Step 2: Based on the determined request type, validation if all mandatory fields are present in the {@link CloudRequest}
	 * Step 3: Get Password 
	 * Step 4: Authenticate and return response
	 */
	@Override
	public CloudResponse authenticate(CloudRequest request) {
		request.setSuppressFields(suppressFields);
		
		//BEGIN - Validation And Preparation
		List<String> mandatoryFieldsForAuthentication = determineAuthenticationTypeAndMandatoryFields(request);
		CloudResponse response = CloudServiceUtil.crossReferenceMandatoryKeysWithCloudRequest(request, mandatoryFieldsForAuthentication);
		if (!response.processed()) {
			String warnMsg = "Authentication Field. Cloud Request Does Not Contain All Mandatory Keys For Authentication. " + response.getMessage();
			logger.printWarn(warnMsg, name);
			return CloudResponse.buildErrorCloudResponse(request, BAD_REQUEST.code(), BAD_REQUEST.name(), warnMsg);
		}
		//END - Validation And Preparation
		
		//Get Password
		response = getPassword(request);
		if (!response.processed()) return response;
		
		//Authenticate
		return authenticate(request, response.getValueByKey(PASSWORD.getText()));
	}
	
	@Override
	public void setLogger(CloudLogger logger) {this.logger = logger;}

	@Override
	public String getCloudSRN() { return name;}
	
	/****------------------------------STARTED - Helper Method For Managing CloudRequest Request Type----------------------------***
	 * ***********************************************************************************************************************************/	
	private CloudResponse authenticate(CloudRequest request, Object password) {
		if(password == null) 
			return buildAndPrintAuthenticationWarning(request, "Authentication Field. Invalid User Id [" 
					+ request.getValueByKeyIgnoreCase(USER_ID.getText()) + "]", UNAUTHORIZED);
		
		String authType = request.getValueByKeyIgnoreCase(AUTH_TYPE.getText());
		if(authType == null) authType = USER_AND_PASSWORD.getText();
		
		if(authType.equalsIgnoreCase(USER_ID_ONLY.getText()))
			return CloudResponse.buildCloudResponse(request, true, SUCCESS.name());
		
		String request_passworld = request.getValueByKeyIgnoreCase(PASSWORD.getText());
		if(request_passworld.equalsIgnoreCase(password.toString()))
			return CloudResponse.buildCloudResponse(request, true, SUCCESS.name());
		
		return buildAndPrintAuthenticationWarning(request, "User Id/Password Does Not Match", UNAUTHORIZED);	
	}
	
	private CloudResponse cache(CloudRequest request, String credentialStore) {
		 try {  
	    	//Fetch and De-crypt Secret
		 	GetSecretValueRequest secretRequest = new GetSecretValueRequest().withSecretId(credentialStore);
	    	GetSecretValueResult secretResult =  secretManagerClient.getSecretValue(secretRequest);
	    	String secret = decreypt(secretResult);
	    	
	    	//Build Cache
	    	JsonNode root = CloudServiceUtil.transformObjectToJsonNode(secret);
			if (root == null || root.fields() == null) 
				return buildAndPrintAuthenticationWarning(request, "Data Corruption. Credentials Stored Against Key [" + credentialStore + "] Have Been Corrupted", INTERNAL_ERROR);
			ObjectMapper objectMapper = new ObjectMapper();
	        Map<?,?> keyValueMap = objectMapper.convertValue(root, Map.class);
	        Map<String, String> cacheMap = new HashMap<String, String>();
	        for(Map.Entry<?, ?> entry: keyValueMap.entrySet()) {
	        	if(entry!= null && entry.getKey() != null && entry.getValue() != null)
	        		cacheMap.put(entry.getKey().toString(), entry.getValue().toString());
	        }
			keyValuePair.put(credentialStore, cacheMap);
			
			return new CloudResponse();
	    } catch (DecryptionFailureException e) {
	    	return buildAndPrintAuthenticationWarning(request, "Could Not Decrypt Secret. Decryption Error [" + e.getMessage() + "]", UNAUTHORIZED);
	    } catch (InternalServiceErrorException e) {
	        return buildAndPrintAuthenticationWarning(request, "AWS Secret Manager Service Error. Error [" + e.getMessage() + "]", INTERNAL_ERROR);
	    } catch (InvalidParameterException e) {
	        return buildAndPrintAuthenticationWarning(request, "Invalid Parameter Received. Error [" + e.getMessage() + "]", BAD_REQUEST);
	    } catch (InvalidRequestException e) {
	    	return buildAndPrintAuthenticationWarning(request, "Invalid Parameter Received. Error [" + e.getMessage() + "]", BAD_REQUEST);
	    } catch (ResourceNotFoundException e) {
	    	return buildAndPrintAuthenticationWarning(request, "Request Secret Does Not Exist. Error [" + e.getMessage() + "]", NOT_FOUND);
	    }catch (Exception e) {
	        return buildAndPrintAuthenticationWarning(request, "AWS Secret Manager Service Error. Error [" + e.getMessage() + "]", INTERNAL_ERROR);
	    }
	}
	
	
	private void initialize(Properties props) {
		secretManagerClient = AWSSecretsManagerClientBuilder.standard().build();
	}

	
	private String decreypt(GetSecretValueResult secretResult) {
		 // Decrypts secret using the associated KMS CMK.
	    // Depending on whether the secret is a string or binary, one of these fields will be populated.
	    if (secretResult.getSecretString() != null) return secretResult.getSecretString();
	    else {
	    	logger.printInfo("Decoding Secret ", name);
	    	return new String(java.util.Base64.getDecoder().decode(secretResult.getSecretBinary()).array());
	    }
	}
	
	private CloudResponse buildAndPrintAuthenticationWarning(CloudRequest request, String warnMsg, StatusCode code) {
		logger.printWarn(warnMsg, name);
		return CloudResponse.buildErrorCloudResponse(request, code.code() , code.name(), warnMsg);
	}
	
	/**This method determine authentication type of the incoming request. If request contains authentication type to be user id only, then only
	 * authentication type is set to user id only, else user and password auth type */
	private List<String> determineAuthenticationTypeAndMandatoryFields(CloudRequest request) {
		String authType = request.getValueByKeyIgnoreCase(AUTH_TYPE.getText());
		if (authType == null || !authType.equalsIgnoreCase(USER_ID_ONLY.getText())) 
				return Arrays.asList(CREDENTIAL_STORE.getText(), USER_ID.getText(), PASSWORD.getText());
		
		return Arrays.asList(CREDENTIAL_STORE.getText(), USER_ID.getText());
	}

}
