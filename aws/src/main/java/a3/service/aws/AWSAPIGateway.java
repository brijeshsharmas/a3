/**
 * 
 */
package a3.service.aws;


import static a3.service.aws.AWSServiceFactory.Service.*;
import static cloud.request.accelerator.CloudRequestAccelerator.Config.SERVICE_ARNS;
import static cloud.service.CloudService.Property.CLOUD_PROVIDER_SUBSITUTOR;
import static cloud.service.CloudService.Property.CLOUD_SERVICE_OPERATION;
import static cloud.service.CloudService.Property.SERVICE_NAME_SUBSITUTOR;

import static cloud.service.CloudService.StatusCode.NOT_FOUND;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

import ca.ryangreen.apigateway.generic.GenericApiGatewayClient;
import ca.ryangreen.apigateway.generic.GenericApiGatewayClientBuilder;
import ca.ryangreen.apigateway.generic.GenericApiGatewayException;
import ca.ryangreen.apigateway.generic.GenericApiGatewayRequestBuilder;
import ca.ryangreen.apigateway.generic.GenericApiGatewayResponse;
import cloud.model.RestApiResponse;
import cloud.model.request.CloudRequest;
import cloud.model.request.CloudResponse;
import cloud.service.CloudLogger;
import cloud.service.CloudRestAPIService;
import cloud.service.CloudServiceException;
import cloud.service.CloudServiceUtil;

/**
 * @author Brijesh Sharma
 *
 */
public class AWSAPIGateway implements CloudRestAPIService {
	
	private String name = getClass().getSimpleName();
	private String endpoint = null;
	private String region = null;
	private CloudLogger logger = null;
	private Properties props = null;
	
	GenericApiGatewayClient client = null;
	
	private String serviceExceptionOperation = CLOUD_SERVICE_OPERATION.getText().
			replaceFirst(CLOUD_PROVIDER_SUBSITUTOR.getText(), "AWS").
			replaceFirst(SERVICE_NAME_SUBSITUTOR.getText(), API_GATEWAY.name());

	/**---------------------Constructors----------------------------------*/
	public AWSAPIGateway(Properties props) { initialize(props);}

	/**---------------------{@link CloudRestAPIService} method implementation----------------------------------*/
	@Override
	public void setLogger(CloudLogger logger) {this.logger = logger;}

	@Override
	public String getCloudSRN() { return name; }

	@Override
	public CloudResponse execute(CloudRequest request) {
		
		CloudResponse response = CloudServiceUtil.crossReferenceMandatoryKeysWithCloudRequest(request, listMandatoryKeyForExecution());
		if (!response.processed()) {
			logger.printWarn(name + "Aborting Rest API Execution Because " + response.getMessage() , name);
			return response;
		}
		
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(Key.CONTENT_TYPE.getText(), request.getValueByKeyIgnoreCase(Key.CONTENT_TYPE.getText()));
		
		try {
			String body = request.getValueByKeyIgnoreCase(Key.BODY.getText());
			HttpMethodName method = HttpMethodName.fromValue(request.getValueByKeyIgnoreCase(Key.HTTP_METHOD.getText()));
			GenericApiGatewayResponse apiGatewayResponse = client.execute(
		            new GenericApiGatewayRequestBuilder()
		                    .withBody(new ByteArrayInputStream((body == null ? "" : body).getBytes()))
		                .withHttpMethod(method)
		                .withHeaders(headers)
		                .withResourcePath(request.getValueByKeyIgnoreCase(Key.RESOURCE.getText())).build());
			
			RestApiResponse apiResponse = new RestApiResponse(apiGatewayResponse.getBody(), apiGatewayResponse.getHttpResponse().getHeaders(), 
					apiGatewayResponse.getHttpResponse().getStatusCode(), true);
		    return CloudResponse.buildCloudResponse(request, apiResponse, true);
		} catch (GenericApiGatewayException e) { 
			String errMsg = String.format("Client threw exception with message %s and status code %s", 
				            e.getMessage(), e.getStatusCode());
			logger.printErr(errMsg, name);
			//return CloudResponse.buildCloudResponse(request, false, errMsg);
			return CloudResponse.buildErrorCloudResponse(request, NOT_FOUND.code() , NOT_FOUND.name(), errMsg);
		} catch (Exception e) { 
			String errMsg = String.format("Client threw exception with message %s", 
				            e.getMessage());
			logger.printErr(errMsg, name);
			return CloudResponse.buildErrorCloudResponse(request, NOT_FOUND.code() , NOT_FOUND.name(), errMsg);
		}
	}
	
	/**------------------Helper Methods Begins Here----------------------------------------------------------------------*/
	private void initialize(Properties props) {
		this.props = props;
		checkMandatoryConfig_ThrowErrorIfMissing(this.props);
		
		endpoint = CloudServiceUtil.getPropertyIgnoreCase(props,SERVICE_ARNS.getText());
		region = CloudServiceUtil.getPropertyIgnoreCase(props,Key.REGION.name());
		
		client = new GenericApiGatewayClientBuilder()
		        .withClientConfiguration(new ClientConfiguration())
		        .withCredentials(new EnvironmentVariableCredentialsProvider())
		        .withEndpoint(endpoint)
		        .withRegion(Region.getRegion(Regions.fromName(region)))
		        .build();
	}
	
	private void checkMandatoryConfig_ThrowErrorIfMissing(Properties props) {
		throwErrorIfMissing(props, SERVICE_ARNS.name());
		throwErrorIfMissing(props, Key.REGION.name());
	}
	
	private void throwErrorIfMissing(Properties props, String key) {
		if ( CloudServiceUtil.getPropertyIgnoreCase(props, key) == null ) {
			String errMsg = "Cloud Not Initialize Cloud Service[" + name + "] Due To Missing Mandatory Configuration [" + key + "]";
			logger.printErr(errMsg, name);
			throw new CloudServiceException(serviceExceptionOperation, errMsg);
		}
	}
}

