/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.service.irs;


import static a3.service.irs.IrsTemplate.IrsSchema.GATHER;
import static a3.service.irs.IrsTemplate.IrsSchema.GATHER_DIGITS;
import static a3.service.irs.IrsTemplate.IrsSchema.GATHER_RESPONSE;
import static a3.service.irs.IrsTemplate.IrsSchema.GATHER_RESPONSE_APPEND;
import static a3.service.irs.IrsTemplate.IrsSchema.OVERRIDES;
import static a3.service.irs.IrsTemplate.IrsSchema.SAY;
import static a3.service.irs.IrsTemplate.ResponseType.INVALID_RESPONSE_TYPE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import a3.model.request.A3Request;
import a3.service.A3Logger;
import a3.service.A3ServiceException;
import a3.service.A3ServiceUtil;
import a3.service.irs.IrsTemplate;
import a3.service.irs.IrsService.Type;


public class IrsTemplateJsonImpl implements IrsTemplate {
	protected String name = getClass().getSimpleName();
	
	private String say = null;
	private String gather = null;
	
	private A3Logger logger = null;
	
	private String templateName = null;
	private int numGatherDigits = 1;
	private String responseDataType = null;
	private String responseDataFormat = null;
	private Type templateType = null;
	private String templateVersion = null;
	
	private Map<String, String> gatherResponse = new HashMap<String, String>();
	private Map<String, Map<String, String> > overrides = new HashMap<String, Map<String,String>>();
	private List<IrsTemplateJsonAppend> append = new ArrayList<IrsTemplateJsonAppend>();
	
	/**----------------------------------------------CONSTRUCTORS--------------------------------------------------**/
	public IrsTemplateJsonImpl() {}
	public IrsTemplateJsonImpl(int numGatherDigit, String say, String gather, Map<String, String> response, List<IrsTemplateJsonAppend> append, 
			Map<String, Map<String, String> > overrides, String templateName, String responseType, String responseFormat, Type templateType) {
		this.numGatherDigits = numGatherDigit;
		this.say=say;
		this.gather= gather;
		this.gatherResponse =response;
		this.append = append;
		this.overrides = overrides;
		this.templateName = templateName;
		this.responseDataType = responseType;
		this.responseDataFormat = responseFormat;
		this.templateType = templateType;
	}

	/***---------------------------STARTED--{@link IrsTemplate} methods-------------------------------------------------
	 * ---------------------------------------------------------------------------------------------------------------**/
	@Override
	public String buildSay(Object response, A3Request request) {
		//IrsTemplate IMPLEMENTATION TYPES SUCH AS IVR OR EMAIL MUST OVERRIDE THIS METHOD TO PROVIDE TYPE SPECIFIC IMPLEMENTATION
		return null;
	}
	
	@Override
	public String buildGather(Object response, A3Request request) {
		//IrsTemplate IMPLEMENTATION TYPES SUCH AS IVR OR EMAIL MUST OVERRIDE THIS METHOD TO PROVIDE TYPE SPECIFIC IMPLEMENTATION
		return null;
	}
	
	@Override
	public int getNumGatherDigits() {return numGatherDigits;}
	
	@Override
	public Type getTemplateType() { return templateType; }
	
	@Override
	public String getTemplateVersion() { return templateVersion; }
	
	@Override
	public boolean isValidResponse(Object response) {
		if (response == null) return false;
		
		ResponseType formatType = determineResponseDataType(getResponseDataType());
		switch (formatType) {
		case RESPONSE_TYPE_STRING:
			return getResponse().containsKey(response);
			
		case RESPONSE_TYPE_NUMBER:
			if (!getResponse().containsKey(response)) return false;
			if (! A3ServiceUtil.isNumeric(response.toString())) return false;
			return true;

		case RESPONSE_TYPE_DATE:
			if (getResponse().containsKey(response)) return true;
			return A3ServiceUtil.isDate(response.toString(), getResponseDataFormat());
			
		default:
			throw new A3ServiceException("Response Determination", "Template [" + getTemplateName() + "] Response Data Type ["+ getResponseDataType() 
				+ "] Is Not A Valid Response Type");
		}
	}
	
	@Override
	public void setLogger(A3Logger logger) { this.logger = logger; }
	
	@Override
	public boolean equals(final Object other) {
		if (this == other)  return true;
		if (other == null) return false;
		if (!getClass().equals(other.getClass())) return false;
	
		IrsTemplateJsonImpl castOther = (IrsTemplateJsonImpl) other;
		return Objects.equals(say, castOther.say)
				&& Objects.equals(gather, castOther.gather)
				&& Objects.equals(gatherResponse, castOther.gatherResponse)
				&& Objects.equals(append, castOther.append);
	}
	
	@Override
	public int hashCode() { return Objects.hash(say, gather, gatherResponse, append); }
	
	@Override
	public String toString() {
		return name + " [" + SAY.getText() + "=" + say + ", "  + GATHER.getText() + "=" + gather + ", "  + 
				GATHER_RESPONSE.getText() + "=" + gatherResponse + ", " +  GATHER_RESPONSE_APPEND.getText() + "=" + append + 
					"," + OVERRIDES.getText() + "=" + overrides + "," +  GATHER_DIGITS.getText() + "=" + numGatherDigits + "]";
	}
	
	@Override
	public String getTemplateName() {return templateName;}	
	
	@Override
	public Map<String, String> getResponse() {	return gatherResponse;}
	
	@Override
	public List<String> listAllOverride() {
		List<String> listOverrides = new ArrayList<String>();
		Map<String, Map<String, String> > mapOverrides = getOverrides();
		for(Map.Entry<String, Map<String, String>> root: mapOverrides.entrySet()) {
			for(Map.Entry<String, String> entry: root.getValue().entrySet()) {
				if(!listOverrides.contains(entry.getValue()))
					listOverrides.add(entry.getValue());
			}
		}
		
		List<IrsTemplateJsonAppend> listAppends = getAppend();
		for(IrsTemplateJsonAppend dialAppend: listAppends) {
			if (!listOverrides.contains(dialAppend.getField())) listOverrides.add(dialAppend.getField());
		}
	
		return listOverrides;
	}
	
	/***---------------------------STARTED--Helper methods For Validation------------------------------------------------
	 * ---------------------------------------------------------------------------------------------------------------**/
	protected String getResponseMessage(Object response) {
		if (response == null) return null;
		ResponseType formatType = determineResponseDataType(getResponseDataType());
		
		switch (formatType) {
		case RESPONSE_TYPE_NUMBER:
			return getResponse().get(response);

		case RESPONSE_TYPE_STRING:
			return getResponse().get(response);
			
		case RESPONSE_TYPE_DATE://For date it will either return item with key = 00 or first item;
			String responseMessage = getResponse().get(response);
			if (responseMessage == null) {
				Map<String, String> responses = getResponse();
				if(responses.size() > 0) {
					for(Map.Entry<String, String> entry: responses.entrySet())
						return entry.getValue();
				}
			}
			return responseMessage;
			
		default:
			throw new A3ServiceException("GetResponseMessage", "Template [" + getTemplateName() + "] Response Format [" + getResponseDataFormat() + "] Is Not A Valid Format");
		}
	}
	
	protected ResponseType determineResponseDataType(String format) {
		if (format == null) return INVALID_RESPONSE_TYPE;
		format = format.trim().toLowerCase();
		
		for(ResponseType key: ResponseType.values()) {
			if (key.getText().equalsIgnoreCase(format))
				return key;
		}
		
		return INVALID_RESPONSE_TYPE;
	}
	
	protected String applyOverrides_ThrowErrorIfMissing(Map<String, String> listOverrides, A3Request request, String message) {
		List<String> missingOverrides = new ArrayList<String>();
		if (listOverrides == null || request == null) return message;
		
		for (Map.Entry<String, String> entry: listOverrides.entrySet()) {
			String overrideValue = request.getValueByKeyIgnoreCase(entry.getValue());
			if (overrideValue == null || overrideValue.trim().length() == 0) missingOverrides.add(entry.getValue());
			else message = message.replaceAll(entry.getKey(), overrideValue);
		}
		
		if (missingOverrides.size() == 0) return message;
		
		String missingOverrrides = "Missing Override " + missingOverrides + " For Say Message";
		String errMsg = "Could Not Build Message Due To Missing Override [" + missingOverrides + "] For [" +
				message + "] In The Cloud Request [" + request + "]";
		logger.printErr(errMsg, name);
		throw new A3ServiceException("", missingOverrrides);
	}
	
	
	/***---------------------------STARTED--Helper methods For Enrichment------------------------------------------------
	 * ---------------------------------------------------------------------------------------------------------------**/
	protected String applyAppend(Object response, A3Request request, String message) {
		List<IrsTemplateJsonAppend> listAppend = getAppendListByResponse(response);
		for (IrsTemplateJsonAppend append: listAppend) {
			String field = append.getField();
			Object value = append.getValue();
			if (field == null || value == null) continue;
			
			String valueFromRequestObject = request.getValueByKeyIgnoreCase(field);
			if (valueFromRequestObject != null) {
				if (valueFromRequestObject.equalsIgnoreCase(value.toString()))
					message += append.getMessage();
			}
		}
		return message;
	}
	
	/***---------------------------STARTED--Getter/Setter Methods------------------------------------------------
	 * ---------------------------------------------------------------------------------------------------------------**/
	public void addGatherResponse(String digit, String message) {gatherResponse.put(digit, message);}
	public String getGatherResponseByDigit(String digit) { return gatherResponse.get(digit); }
	public void addResponseAppend(String digit, String field, Object value, String message) {
		append.add(new IrsTemplateJsonAppend(digit, field, value, message)); }
	public void addResponseAppend(IrsTemplateJsonAppend appendResponse) { append.add(appendResponse); }
	public List<IrsTemplateJsonAppend> getAppendListByResponse(Object response) { 
		List<IrsTemplateJsonAppend> listAppend = new ArrayList<IrsTemplateJsonAppend>();
		for (IrsTemplateJsonAppend item: append) {
			if (response != null && item!= null && item.getResponse() != null && item.getResponse().equalsIgnoreCase(response.toString())) 
				listAppend.add(item);
		}
		return listAppend; 
	}
	public void addOverrides(String schema, Map<String, String> listOverrides) {
		overrides.put(schema, listOverrides);
	}
	public Map<String, String> getOverridesBySchema(String schema) { return overrides.get(schema);}
	public String getSay() {return say;}
	public String getGather() {	return gather;}
	public List<IrsTemplateJsonAppend> getAppend() {	return append;}
	public Map<String, Map<String, String> > getOverrides() { return overrides; }
	public String getResponseDataFormat() { return responseDataFormat; }
	public String getResponseDataType() { return responseDataType; }
	
	public void setOverrides(Map<String, Map<String, String> > overrides) { this.overrides = overrides; }
	public void setResponse(Map<String, String> response) {	this.gatherResponse = response;}
	public void setGather(String gather) {this.gather = gather;}
	public void setTemplateName(String templateName) {this.templateName = templateName;}
	public void setSay(String say) {this.say = say;}
	public void setAppend(List<IrsTemplateJsonAppend> append) {	this.append = append;}
	public void setNumGatherDigit(int numGatherDigit) { this.numGatherDigits = numGatherDigit; }
	public void setResponseDataType(String responseType) { this.responseDataType = responseType; }
	public void setResponseDataFormat(String responseFormat) { this.responseDataFormat = responseFormat; }
	public void setTemplateType(Type templateType) {this.templateType = templateType; }
	public void setTemplateVersion(String templateVersion) { this.templateVersion = templateVersion; }
	public String toJsonString() {
		try {return A3ServiceUtil.transformObjectToJsonString(this); } catch(Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	}
