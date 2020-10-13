/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.service.irs.ivr;

import static a3.service.irs.IrsTemplate.IrsSchema.GATHER;
import static a3.service.irs.IrsTemplate.IrsSchema.SAY;
import static a3.service.irs.ivr.IvrService.IvrRecord.TO_PHONE;

import java.util.List;
import java.util.Map;

import a3.model.request.A3Request;
import a3.service.irs.IrsTemplateJsonAppend;
import a3.service.irs.IrsTemplateJsonImpl;
import a3.service.irs.IrsService.Type;

public class IvrTemplateJsonImpl extends IrsTemplateJsonImpl implements IvrTemplate {
	protected String name = getClass().getSimpleName();
	private String finishOnKey = null;
	private String textToSpeechActor = null;
	
	/**----------------------------------------------CONSTRUCTORS--------------------------------------------------**/
	public IvrTemplateJsonImpl() {}
	public IvrTemplateJsonImpl(int numGatherDigit, String say, String gather, Map<String, String> response, List<IrsTemplateJsonAppend> append, 
			Map<String, Map<String, String> > overrides, String templateName, String responseType, String responseFormat, Type templateType, 
			String finishOnKey, String textToSpeechActor) {
		super(numGatherDigit, say, gather, response, append, overrides, templateName, responseType, responseFormat, templateType);
		
		this.finishOnKey = finishOnKey;
		this.textToSpeechActor = textToSpeechActor;
	}

	/***---------------------------STARTED--{@link DialTemplate} methods-------------------------------------------------
	 * ---------------------------------------------------------------------------------------------------------------**/
	@Override
	public String buildSay(Object response, A3Request request) {
		String say = null;
		//Condition 1: When Ivr was called either without response or doReplay * value
		if (response == null || response.toString().trim().length() == 0 || doReplayMessage(response)) 
			say = getSay();
		
		//Condition 2. Response was sent, however, it is not a valid response
		else if (!isValidResponse(response)) say = buildInvalidResponseOptionMessage(response);//Invalid Response Format Message
		
		//Condition 3. It was a valid response message, get corresponding say message for the response
		if(say == null) say = getResponseMessage(response);//Say message based on the response
		
		if (say == null) say = buildInvalidResponseOptionMessage(response);//Invalid Say Message
		else say = applyAppend(response, request, say);

		return applyOverrides_ThrowErrorIfMissing(getOverridesBySchema(SAY.getText()), request, say);
	}
	
	@Override
	public String buildGather(Object response, A3Request request) {
		/**Return gather message for below condition
		response == null || response.length == 0 (this indicate that this is the first time request)
		getResponse().get(response) == null (this indicate user selected invalid option, hence replay message will be played)
		*/
		if (response == null || response.toString().trim().length() == 0 || doReplayMessage(response) || !isValidResponse(response)) 
				return applyOverrides_ThrowErrorIfMissing(getOverridesBySchema(GATHER.getText()), request, getGather());
		
		//Special handling for Data type format. 
		//For cases, where calle has input for help, there will a corresponding matching response item in response map
		//Hence, gather needs to be played again irrespective of a valid response is available
		ResponseType formatType = determineResponseDataType(getResponseDataType());
		if (formatType.equals(ResponseType.RESPONSE_TYPE_DATE) && getResponse().containsKey(response))
			return applyOverrides_ThrowErrorIfMissing(getOverridesBySchema(GATHER.getText()), request, getGather());
		
		return null;
	}
	
	@Override
	public String getFinishOnKey() { return finishOnKey; }
	
	@Override
	public String getTextToSpeechActor() { return textToSpeechActor; }
	
	@Override
	public List<String> listAllOverride() {
		List<String> overrideList = super.listAllOverride();
		overrideList.add(TO_PHONE.getText());
		return overrideList;
	}
	
	public void setTextToSpeechActor(String textToSpeechActor) { this.textToSpeechActor = textToSpeechActor; }
	public void setFinishOnKey(String finishOnKey) { this.finishOnKey = finishOnKey; }
	
	}
