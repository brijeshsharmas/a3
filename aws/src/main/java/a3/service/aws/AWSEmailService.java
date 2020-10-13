/**
 * 
 */
package a3.service.aws;

import cloud.model.Envelope;
import cloud.service.CloudEmailService;
import cloud.service.CloudLogger;
import cloud.service.CloudServiceException;
import cloud.service.CloudServiceUtil;

import static a3.service.aws.AWSServiceFactory.Service.SES;
import static cloud.request.accelerator.CloudRequestAccelerator.Config.SERVICE_ARNS;
import static cloud.service.CloudService.Property.CLOUD_PROVIDER_SUBSITUTOR;
import static cloud.service.CloudService.Property.CLOUD_SERVICE_OPERATION;
import static cloud.service.CloudService.Property.SERVICE_NAME_SUBSITUTOR;
import java.util.ArrayList;
import java.util.Properties;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;

/**
 * @author Brijesh Sharma
 *
 */
public class AWSEmailService implements CloudEmailService {
	
	private String name = getClass().getSimpleName();
	private CloudLogger logger = null;
	
	private Regions region = null;
	AmazonSimpleEmailService client = null;
	
	private String serviceExceptionOperation = CLOUD_SERVICE_OPERATION.getText().
			replaceFirst(CLOUD_PROVIDER_SUBSITUTOR.getText(), "AWS").
			replaceFirst(SERVICE_NAME_SUBSITUTOR.getText(), SES.name());

	/*---------------------Constructors----------------------------------*/
	public AWSEmailService(Properties props) { initialize(props);	}
	
	@Override
	public Envelope send(Envelope envelope) {
		try {
			
			Body body = new Body();
			Content content = null;
			
			if (envelope.getTxtBody() != null ) { 
				content = new Content()
						.withCharset("UTF-8")
						.withData(envelope.getTxtBody());
				body = body.withText(content);
			}
			if (envelope.getHtmlBody() != null ) { 
				content = new Content()
						.withCharset("UTF-8")
						.withData(envelope.getHtmlBody());
				body = body.withHtml(content);
			}
			
			SendEmailRequest request = new SendEmailRequest()
					.withDestination(new Destination().withToAddresses(envelope.getToList()))
					.withMessage(new Message().withBody(body).withSubject(new Content().withCharset("UTF-8").withData(envelope.getSubject())))
					.withSource(envelope.getFrom());
					//.withConfigurationSetName(CONFIGSET);
			client.sendEmail(request);
			logger.printInfo("Email Succesfully Sent From [" + envelope.getFrom() + "] To " + envelope.getToList(), name);
		} catch (Exception ex) {
			logger.printInfo("Error Sending From [" + envelope.getFrom() + "] To " + envelope.getToList() + ". Error [" + ex.getMessage() + "]", name);
			envelope.setSuccess(false);
			envelope.setErrMsg(ex.getMessage());
		}
		return envelope;
	}

	@Override
	public void setLogger(CloudLogger logger) { this.logger = logger;}

	@Override
	public String getCloudSRN() { return region == null ? null : region.getName(); }

	

	/**------------------Helper Methods Begins Here----------------------------------------------------------------------*/
	private void initialize(Properties props) {
		String strRegion = props.getProperty(SERVICE_ARNS.getText());
		if (strRegion == null) throw new CloudServiceException(serviceExceptionOperation, 
				"Could Not Find Service ARN In The Property Argument");
		
		region = CloudServiceUtil.getEnumTypeIgnoreCase(strRegion, Regions.class);
		if (region == null) throw new CloudServiceException(serviceExceptionOperation, 
				"Invalid Service ARN In The Property Argument. Region [" + strRegion + "] Is Not A Valid AWS Region");
		
		client = AmazonSimpleEmailServiceClientBuilder.standard().withRegion(region).build();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		//String htmlBody = "Please click <a href='https://x6jydtc0nl.execute-api.us-east-1.amazonaws.com/prod/IvrServiceDispatcher?client_id=hiring&id=Ivrlhaphkqohyajztd&client_password=hiring123&REQUEST_TYPE=GET_IVR_STATUS'> yes </a> ";
		String url = "https://sho9x694i8.execute-api.us-east-1.amazonaws.com/dev/IvrServiceDispatcher?client_id=hiring&id=Ivrlhaphkqohyajztd&client_password=hiring123&REQUEST_TYPE=GET_IVR_STATUS";
		String htmlBody = "<html><form action=\"" + url + "\" method=\"post\">"; 
	    htmlBody += "<a href=\"javascript:;\" onclick=\"parentNode.submit();\">Please Yes</a>";
	    htmlBody += "<input type=\"hidden\" name=\"mess\" value=/></form></html>";
		
	  htmlBody = "Please click <a href='https://sho9x694i8.execute-api.us-east-1.amazonaws.com/dev/IvrServiceDispatcher?client_id=hiring&id=Ivrlhaphkqohyajztd&client_password=hiring123&REQUEST_TYPE=GET_IVR_STATUS'> yes </a> ";
		
//	    System.out.println(htmlBody);
//	    
//	    if (true) return;
	    
		Properties props = new Properties();
		props.setProperty(SERVICE_ARNS.getText(), "us_east_1");
		AWSEmailService service = new AWSEmailService(props);
		service.setLogger(new CloudLogger());
		ArrayList<String> listTo = new ArrayList<String>();
		listTo.add("brijeshsharmas@gmail.com");
		Envelope email = new Envelope("brijesh.sharma1@publicissapient.com", listTo, "AWS Test Email", "Text Body", htmlBody);
		email = service.send(email);
		System.out.println(email.isSuccess());

	}

}
