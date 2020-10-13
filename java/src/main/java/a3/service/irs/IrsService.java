/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.service.irs;

import java.util.List;

import a3.model.request.A3Request;
import a3.model.request.A3Response;
import a3.service.A3Service;
import a3.service.A3ServiceUtil;
import a3.service.irs.ivr.IvrService;

/**
 * @author Brijesh Sharma<br>
 * This is the highest level of interface for interactive services, which primarily uses for initiating an interactive session with user over medium such as 
 * phone, sms, email etc..
 *
 */
public interface IrsService extends A3Service {

	public enum AuthKey{
		IRS_CLIENT_ID("client_id"),
		IRS_CLIENT_PWD("client_password"),
		IRS_VENDOR_ID("vendor_id"),
		IRS_VENDOR_PWD("vendor_password");
		private String text;
		private AuthKey(String text){ this.text = text; }
		public String getText() { return text; }
		public void setText(String text) { this.text = text; }
	};
	
	public enum Type{
		IRS("irs"), IVR("ivr"), EMAIL("email");
		private String text;
		private Type(String text){ this.text = text; }
		public String getText() { return text; }
		public void setText(String text) { this.text = text; }
	};
	
	public enum IrsRecord{
		ID("id"), 
		EXTERNAL_ID("external_id"),
		SOURCE("source"),
		EXPIRED_ON("expired_on"),
		IRS_TYPE("irs_type"),
		
		STATUS("irs_status"),
		FAILED_STATUS_REASON("irs_status_failed_reason"),
		FAILED_STATUS("failed"),
		VALIDATION_FAILED_STATUS("validation_failed"),
		POSTED_STATUS("posted"),
		STATUS_COMPLETED("completed"),
		
		
		ATTEMPT("attemps"),
		LAST_ATTEMPT_DATE_TIME("last_attempt_date_time"),
		
		RESPONSE("irs_response"),
		PREVIOUS_RESPONSE("irs_previous_response"),
		RESPONSE_DATE_TIME("response_date_time"),
		
		CREATED_DATE_TIME("created_date_time"),
		UPDATED_DATE_TIME("updated_date_time"),
		
		ACCOUNT("account"),
		TEMPLATE("template"),
		TEMPLATE_TYPE("template_type"),
		
		SYNCHED("synched"),
		SYNCHED_STATUS_YES("yes"),
		SYNCHED_STATUS_NO("no")
		;

		private String text;
		private IrsRecord(String text){ this.text = text; }
		public String getText() { return text; }
		public void setText(String text) { this.text = text; }
	};
	
	
	
	/**This method return Ivr Call status for given object id*/
	public A3Response getStatus(A3Request request);
	
	/**This method process given {@link A3Request}, which contain Irs call/email details and return call execution status in {@link A3Response}*/
	public A3Response process(A3Request request);
	
	/**Method use for generate Irs id*/
	default String generateId() {
		return "Irs" + A3ServiceUtil.generateRandonAlphaNumberic(15, 15, "");
	}
	
	/**Method use for generate Irs id*/
	default Type getType() {
		return Type.IRS;
	}

	/**This method builds IvrSchema, which can be processed by an {@link IvrService}*/
	public Object buildMessage(TemplateService templateService, A3Request request);
	
	/**return list of mandatory {@link Dial} configuration fields that are necessary to start an ivr call*/
	public default String[] listMandatoryFieldsForPostingRequest(IrsTemplate irsTemplate) {
		int irsFieldsLength = 2;
		int irsTemplateOverrideLength = irsTemplate == null ? 0 : (irsTemplate.listAllOverride() == null ? 0 : irsTemplate.listAllOverride().size());
		
		String [] fields = new String[irsFieldsLength + irsTemplateOverrideLength];
		fields[0] = IrsRecord.TEMPLATE.getText();
		fields[1] = AuthKey.IRS_CLIENT_ID.getText();
		if (irsTemplateOverrideLength > 0) {
			List<String> listOverrides = irsTemplate.listAllOverride();
			for(String str: listOverrides)
				fields[irsFieldsLength++] = str;
		}
		return fields;
	}
	
	public default String[] listMandatoryFieldsForPreparingCallbackURL(IrsTemplate irsTemplate ) {
		int irsFieldsLength = 3;
		int irsTemplateOverrideLength = irsTemplate == null ? 0 : (irsTemplate.listAllOverride() == null ? 0 : irsTemplate.listAllOverride().size());
		
		String [] fields = new String[irsFieldsLength + irsTemplateOverrideLength];
		fields[0] = IrsRecord.TEMPLATE.getText();
		fields[1] = AuthKey.IRS_CLIENT_ID.getText();
		fields[2] = IrsRecord.ID.getText();
		if (irsTemplateOverrideLength > 0) {
			List<String> listOverrides = irsTemplate.listAllOverride();
			for(String str: listOverrides)
				fields[irsFieldsLength++] = str;
		}
		return fields;
	}
}
