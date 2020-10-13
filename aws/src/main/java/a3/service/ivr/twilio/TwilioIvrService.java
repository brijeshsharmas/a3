
package a3.service.ivr.twilio;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.twilio.Twilio;
import com.twilio.http.HttpMethod;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Gather;
import com.twilio.twiml.voice.Say;
import com.twilio.twiml.voice.Gather.Input;
import com.twilio.twiml.voice.Say.Voice;
import com.twilio.twiml.voice.SsmlProsody;
import com.twilio.type.PhoneNumber;

import cloud.model.request.CloudRequest;
import cloud.model.request.CloudResponse;
import cloud.service.CloudLogger;
import cloud.service.CloudServiceException;
import cloud.service.CloudServiceUtil;
import cloud.service.irs.TemplateService;
import cloud.service.irs.ivr.IvrService;
import cloud.service.irs.ivr.IvrTemplate;

import static cloud.service.CloudService.StatusCode.SUCCESS;
import static cloud.service.irs.IrsService.AuthKey.IRS_VENDOR_ID;
import static cloud.service.irs.IrsService.AuthKey.IRS_VENDOR_PWD;
import static cloud.service.irs.ivr.IvrService.IvrRecord.CALLBACK_URL;
import static cloud.service.irs.ivr.IvrService.IvrRecord.CALL_DURATION;
import static cloud.service.irs.ivr.IvrService.IvrRecord.CALL_END_TIME;
import static cloud.service.irs.ivr.IvrService.IvrRecord.CALL_PRICE;
import static cloud.service.irs.ivr.IvrService.IvrRecord.CALL_PRICE_UNIT;
import static cloud.service.irs.ivr.IvrService.IvrRecord.CALL_START_TIME;
import static cloud.service.irs.IrsService.IrsRecord.EXTERNAL_ID;
import static cloud.service.irs.ivr.IvrService.IvrRecord.FROM_PHONE;
import static cloud.service.irs.IrsService.IrsRecord.ID;
import static cloud.service.irs.ivr.IvrService.IvrRecord.ISO_CODE;
import static cloud.service.irs.IrsService.IrsRecord.RESPONSE;
import static cloud.service.irs.IrsService.IrsRecord.STATUS;
import static cloud.service.irs.IrsService.IrsRecord.SYNCHED;
import static cloud.service.irs.IrsService.IrsRecord.SYNCHED_STATUS_NO;
import static cloud.service.irs.IrsService.IrsRecord.SYNCHED_STATUS_YES;
import static cloud.service.irs.IrsService.IrsRecord.TEMPLATE;
import static cloud.service.irs.ivr.IvrService.IvrRecord.TO_PHONE;
import static cloud.service.CloudService.StatusCode.NOT_FOUND;
import static cloud.service.CloudService.StatusCode.INTERNAL_ERROR;
import static cloud.service.irs.IrsTemplate.IrsSchema.EOM;


/**
 * @author Brijesh Sharma
 *
 */
public class TwilioIvrService implements IvrService {

	private CloudLogger logger = null;
	private String name = getClass().getSimpleName();
	private List<String> listMandatoryFieldsForDialing = Arrays.asList(FROM_PHONE.getText(),
			TO_PHONE.getText(), CALLBACK_URL.getText());
	private List<String> listKeysForCompletingSyncProcess = Arrays.asList(CALL_PRICE.getText());
			
	

	/*---------------Constructors-------------------------------------------------------------------------------------------*/
	public TwilioIvrService(Properties props) {	}
	
	
	/***-----------------------Overriding {@link IvrService} methods---------------------------------------------*/
	@Override
	public CloudResponse process(CloudRequest request) {

		CloudResponse response = new CloudResponse(request);
		
		initialize(request);
		if (!allFieldsPresent_IfNot_BuildCloudResponse(request, response, listMandatoryFieldsForDialing))
			return response;
		
		String fromPhoneNumber = request.getValueByKeyIgnoreCase(FROM_PHONE.getText());
		String toNumber = request.getValueByKeyIgnoreCase(TO_PHONE.getText());
		toNumber = normalizedPhoneNumber(toNumber, request.getValueByKeyIgnoreCase(ISO_CODE.getText()));
		String callBackURL = request.getValueByKeyIgnoreCase(CALLBACK_URL.getText());
		String dialDetails = "From Phone [" + fromPhoneNumber + "], ToPhone [" + toNumber + "], CallBackURL [" + callBackURL + "]" ;
		try {
        	printInfo("Initiating Ivr. Dial Details " + dialDetails);
        	Call call = Call.creator(new PhoneNumber(toNumber), new PhoneNumber(fromPhoneNumber), new URI(callBackURL)).setMethod(HttpMethod.POST).create();
       		String twimlCallSid = call.getSid();
       		String msg = "Succesfully Initiated Ivr Voice Call With Twilio - Twilio CallSid=" +  twimlCallSid;
       		printInfo(msg);
       		response.withKeyValue(EXTERNAL_ID.getText(), twimlCallSid);
       		response.setMessage(msg);
       		response.setCode(SUCCESS.code());
       		response.setShortMessage(SUCCESS.name());
       		return response;
	  	} catch (Exception e) {
			String errMsg = "Posting Ivr To Twilio Failed. Error [" + e.getMessage() + "]";	
	  		printErr(errMsg);
			e.printStackTrace();
			return CloudResponse.buildErrorCloudResponse(request, INTERNAL_ERROR.code(), INTERNAL_ERROR.name(),errMsg);
		}
	}
	
	@Override
	public Object buildMessage(TemplateService templateService, CloudRequest request) {
		IvrTemplate template = validateTemplate_ThrowErrorIfInvalid(templateService, request);
		return buildIvrSchema(template, request);
	}
	
	@Override
	public void setLogger(CloudLogger cloudLogger) { this.logger = cloudLogger;	}

	@Override
	public CloudResponse getStatus(CloudRequest request) {
		
		initialize(request);
		
		CloudResponse response = new CloudResponse();
		String externalId = request.getValueByKeyIgnoreCase(EXTERNAL_ID.getText());
		Call call = null;
		try {
			call = Call.fetcher(externalId).fetch();
		}catch(Exception e) {
			String errMsg = "Could Not Fetch Ivr Call Status For Ivr Id [" + request.getValueByKey(ID.getText()) 
				+ "] And Ivr External Id [" + externalId +  "] By Ivr Service [" + name + "]. Error: [" + e.getMessage() + "]";
			logger.printErr(errMsg, name);
			return CloudResponse.buildErrorCloudResponse(request, NOT_FOUND.code(), NOT_FOUND.name(), errMsg);
		}
		CloudServiceUtil.populateIfNotNull(response, STATUS.getText(), call.getStatus());
		CloudServiceUtil.populateIfNotNull(response, IRS_VENDOR_ID.getText(), call.getAccountSid());
		CloudServiceUtil.populateIfNotNull(response, CALL_DURATION.getText(), call.getDuration());
		CloudServiceUtil.populateIfNotNull(response, FROM_PHONE.getText(), call.getFrom());
		CloudServiceUtil.populateIfNotNull(response, TO_PHONE.getText(), call.getTo());
		CloudServiceUtil.populateIfNotNull(response, CALL_END_TIME.getText(), call.getEndTime());
		CloudServiceUtil.populateIfNotNull(response, CALL_PRICE.getText(), call.getPrice());
		CloudServiceUtil.populateIfNotNull(response, CALL_PRICE_UNIT.getText(), call.getPriceUnit());
		CloudServiceUtil.populateIfNotNull(response, CALL_START_TIME.getText(), call.getStartTime());
		
		for(String str: listKeysForCompletingSyncProcess) {
			if(response.getValueByKey(str) == null) 
				return response.withKeyValue(SYNCHED.getText(), SYNCHED_STATUS_NO.getText());
		}
		
		return response.withKeyValue(SYNCHED.getText(), SYNCHED_STATUS_YES.getText());
	}

	@Override
	public String getCloudSRN() {return name;}

	/****-----------------------------------------------------STARTED - Helper Method For Building Schema----------------------------***
	 * ***********************************************************************************************************************************/
	private Object buildIvrSchema(IvrTemplate ivrTemplate, CloudRequest request ) {
		String response = request.getValueByKeyIgnoreCase(RESPONSE.getText());
		
		Object say = ivrTemplate.buildSay(response, request);
		String sayMessage = (say == null ? "" : say.toString());
		
		Object gather = null;
		if(sayMessage.indexOf(EOM.getText()) < 0)
			gather = ivrTemplate.buildGather(response, request);
		else	
			sayMessage = sayMessage.replaceAll(EOM.getText(), "");
		
		int numGatherDigit = ivrTemplate.getNumGatherDigits();
		String finishOnKey = ivrTemplate.getFinishOnKey();
		String post_submit_url = request.getValueByKeyIgnoreCase(CALLBACK_URL.getText());
		logger.printInfo("Post URL Is " + post_submit_url, name);
		Voice actor = getActorVoice(ivrTemplate.getTextToSpeechActor());
		logger.printInfo("Text To Speech Actor [" + actor + "]", name);
		
		
		
		if (gather == null) {
			return new VoiceResponse.Builder().
			        say(new Say.Builder().ssmlProsody(createSsmlProsody(sayMessage))
			        		.language(Say.Language.EN_US).voice(actor).build()).build().toXml();
		}
		
		
		return new VoiceResponse.Builder()
    	        .say(new Say.Builder().ssmlProsody(createSsmlProsody(sayMessage)).language(Say.Language.EN_US).voice(actor)
	                    .build())
    	            .gather(new Gather.Builder().finishOnKey(finishOnKey).actionOnEmptyResult(true)
    	                .say(new Say.Builder().ssmlProsody(createSsmlProsody(gather.toString())).
    	                		language(Say.Language.EN_US).voice(actor)
    	                    .build()).inputs(Input.DTMF).numDigits(numGatherDigit).timeout(15).
    	                action(post_submit_url).method(HttpMethod.POST).build())
    	            .build().toXml();
	}
	
	
	/****-----------------------------------------------------STARTED - Helper Method For Initializing Voice Service---------------------***
	 * ***********************************************************************************************************************************/
	private void initialize(CloudRequest request) {
		String password = request.getValueByKeyIgnoreCase(IRS_VENDOR_PWD.getText());
		String userid = request.getValueByKeyIgnoreCase(IRS_VENDOR_ID.getText());
		Twilio.init(userid, password);
		
	}
	/****-----------------------------------------------------STARTED - Helper Method For Checking Validation----------------------------***
	 * ***********************************************************************************************************************************/
	private String normalizedPhoneNumber(String toPhone, String isoCode) {
		if(toPhone == null) return null;
		toPhone = toPhone.trim();
		
		if (isoCode == null || isoCode.trim().length() == 0) 
			return prefixWithPlusSignIfRequired(toPhone);
		
		toPhone = toPhone.replaceAll("+", "");
		return "+" + isoCode.trim() + toPhone;
	}
	private String prefixWithPlusSignIfRequired(String phoneNumber) {
		if(phoneNumber.charAt(0) != '+') return "+" + phoneNumber;
		return phoneNumber;
	}
	private IvrTemplate validateTemplate_ThrowErrorIfInvalid(TemplateService templateService, CloudRequest request) {
		
		if (request == null || request.getValueByKeyIgnoreCase(CALLBACK_URL.getText()) == null) {
			String errMsg = "Aboring Building Ivr Schema Because Voice Config Parameter [" + CALLBACK_URL.getText() + 
					"] Is Null In Request Object [" + request + "]";
			printErr(errMsg);
			throw new CloudServiceException("", errMsg);
		}
		String templateName = request.getValueByKeyIgnoreCase(TEMPLATE.getText());
		if (templateService == null || templateName == null || !(templateService.getIvrTemplate(templateName) instanceof IvrTemplate) ) {
			String errMsg = "Aboring Building Ivr Schema Because Of Invalid Template Service Configuration [Template Service=" + 
					templateService + ", Template Name=" + templateName + ", TemplateConfig From TemplateService=" + 
					templateService.getIvrTemplate(templateName) +"]";
			printErr(errMsg);
			throw new CloudServiceException("", errMsg);
		}
		
		return (IvrTemplate) templateService.getIvrTemplate(templateName);
	}
	private boolean allFieldsPresent_IfNot_BuildCloudResponse(CloudRequest request, CloudResponse response, List<String> mandatoryFields) {
		boolean status = true;
		for(String field: mandatoryFields) {
			if (request.getValueByKeyIgnoreCase(field) == null ) {
				response.setProcessedStatus(false);
				String msg = "\nCould Not Initiate Twilio Ivr Voice Because Mandatory Field [" + 
						field + "] Is Missing For Cloud Request [" + request + "] ";
				response.setMessage(response.getMessage() == null ? msg : response.getMessage() + "\r" + msg );
				status = false;
			}
		}
			
		return status;
	}
	private void printInfo(Object msg) {logger.printInfo(msg, name);}
	private void printErr(Object msg) {logger.printErr(msg, name);}
	public SsmlProsody createSsmlProsody(String message) {
		return new SsmlProsody.Builder(message)
				.pitch("default").rate("medium").volume("x-loud").build();
	            //.pitch("-10%").rate("-15%").volume("x-loud").build();
	}
	private Voice getActorVoice(String actor) {
		return CloudServiceUtil.getEnumTypeIgnoreCase(actor, Voice.class);
	}
	
}
