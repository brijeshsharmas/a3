package a3.request.accelerator.aws;

import java.util.Map;
import java.util.Properties;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.*;

import cloud.request.accelerator.CloudRequestInterceptor;
import cloud.request.accelerator.CloudRequestProcessor;
import cloud.service.CloudLogger;
import cloud.service.CloudServiceUtil;

/**
 * @author <b>Brijesh Sharma</b><br>
 * This is a {@link CloudRequestInterceptor} client implementation for AWS cloud provider for routing AWS Lambda event object to {@link CloudRequestInterceptor}. The primary purpose of this class is to facilitate AWS Lambda
 * sending proper AWS Lambda Runtime event objects such as S3Event, SQSEvent, etc to {@link CloudRequestInterceptor}, which is an interceptor
 * to {@link CloudRequestProcessor} Accelerator.<br>
 * 
 * {@link #handleRequest(Object, Context)} The class initialize {@link CloudRequestInterceptor} using {@link AWSServiceFactory}, {@link CloudLogger} and AWS Lambda Environment variables in the
 * form of Properties object <br> 
*/
public class AWSCloudEventInterceptor extends CloudRequestInterceptor  {
	
	private Properties props = null; 
	private CloudLogger logger = null;
	private String name = getClass().getSimpleName();
	
	public Object handleRequest(Object awsLambdaEventObject, Context awsLambdaContext) {
		
		super.initialize(getLogger(), getProperties());
		return super.processRequest(awsLambdaEventObject, awsLambdaContext);
	}
	
	public Properties getProperties() {
		if (props == null) props = CloudServiceUtil.convertMapToPropertiesIgnoreNull(System.getenv());
		return props;
	}
	
	public CloudLogger getLogger() {
		if (logger == null) logger = new CloudLogger(getProperties());
		return logger;
	}
	
	/** ----HELPER METHOD TO ENSURE AWS LAMBDA SEND PROPER AWS LAMBDA EVENT OBJECT-----------*/
	public Object handleSNSEvent(SNSEvent event, Context context) { return handleRequest(event, context); }
	public Object handleSQSEvent(SQSEvent event, Context context) { return handleRequest(event, context); }
	public Object handleKinesisEvent(KinesisEvent event, Context context) { return handleRequest(event, context); }
	public Object handleS3Event(S3Event event, Context context) { return handleRequest(event, context); }
	public Object handleAPIGatewayProxyRequestEvent(APIGatewayProxyRequestEvent event, Context context) { return handleRequest(event, context); }
	public Object handleDynamoDBStreamEvent(DynamodbEvent event, Context context) { return handleRequest(event, context); }
	public Object handleAPIGatewayResponse(APIGatewayResponse event, Context context) { return handleRequest(event, context); }
	public Object handleMapRequestObject(Map<?, ?> awsMapRequest, Context context) { 	return handleRequest(awsMapRequest, context);}
	public String getName() { return name; }
}
