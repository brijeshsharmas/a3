/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.service.irs;

import java.util.List;
import java.util.Map;

import a3.model.request.A3Request;
import a3.service.A3Logger;

public interface IrsTemplate {
	
	public static enum IrsSchema {
		SAY("say"), GATHER("gather"), GATHER_RESPONSE("response"), GATHER_RESPONSE_APPEND("append"), OVERRIDES("overrides"), 
		GATHER_DIGITS("numGatherDigits"), TEMPLATE_NAME("templateName"), RESPONSE_DATA_TYPE("responseDataType"),
		RESPONSE_DATA_FORMAT("responseDataFormat"), TEMPLATE_TYPE("templateType"), TEMPLATE_VERSION("templateVersion"), EOM("<EOM>");
	
		private String text;
		private IrsSchema(String text){ this.text = text; }
		public String getText() { return text; }
		public void setText(String text) { this.text = text; }
	};
	
	/**----------------------------------------------Enums--------------------------------------------------**/
	public enum ResponseType {
		RESPONSE_TYPE_DATE("Date"),
		RESPONSE_TYPE_NUMBER("Number"),
		RESPONSE_TYPE_STRING("String"),
		INVALID_RESPONSE_TYPE("InvalidResponseType");
		private String text;

		private ResponseType(String text){ this.text = text; }
		public String getText() { return text; }
		public void setText(String text) { this.text = text; }
	};
	
	/**Build a say message using inputs {@link A3Request} <code>request</code> and optional <code>response</code>*/
	public String buildSay(Object response, A3Request request);
	
	/**Build a gather message using inputs {@link A3Request} <code>request</code> and optional <code>response</code>*/
	public String buildGather(Object response, A3Request request);
	
	/**This method is primarily used by {@link IrsTemplateService} to build an {@link IvrTemplate} using argument <code>json</code>*/
	//public IrsTemplate build(Object json);
	
	/**Returns true/false if a given <code>response</code> is a valid response value for this template*/
	public boolean isValidResponse(Object response);

	/**Set logger so {@link EmailTemplate} can log messages*/
	public void setLogger(A3Logger logger);
	
	/**List all overrides of a given template*/
	public List<String> listAllOverride();
	
	/**------------------------Template Getter Methods For Template Properties-----------------------------------------*/
	public String getSay();
	public String getGather();
	public int getNumGatherDigits();
	public String getTemplateName();
	public IrsService.Type getTemplateType();
	public String getTemplateVersion();
	public String getResponseDataFormat();
	public String getResponseDataType();
	public Map<String, String> getResponse();
	/**------------------------Template Getter Methods For Template Properties-----------------------------------------*/	

}
