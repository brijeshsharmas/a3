/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.service.irs.ivr;

import java.util.List;

import a3.service.A3ServiceUtil;
import a3.service.irs.IrsService;
import a3.service.irs.IrsTemplate;

/**
 * @author <b>Brijesh Sharma</b><br>
 * This interface defines contract for an Interactive Voice service, which can initiate and support an Ivr call. The interface contain enums as below<br>
 * {@link KeyName} - defines keys that could be used for configuration purposes<br>
 * {@link Default} - defines default values that could be used for configuration purposes<br>
 */
public interface IvrService extends IrsService {
	
	public static enum KeyName{KEY_MAX_RETRY_ALLOWED, KEY_DATE_FORMAT, KEY_VALID_ACCOUNT; };
	
	public enum IvrRecord{
		FROM_PHONE("from_phone"),
		TO_PHONE("to_phone"), 
		ISO_CODE("iso_code"),
		
		CALLBACK_URL("callback_url"),

		CALL_DURATION("call_duration"),
		CALL_START_TIME("call_start_time"),
		CALL_END_TIME("call_end_time"),
		CALL_PRICE("call_price"),
		CALL_PRICE_UNIT("call_price_unit"),
		;

		private String text;
		private IvrRecord(String text){ this.text = text; }
		public String getText() { return text; }
		public void setText(String text) { this.text = text; }
	};
	
	public enum Default{
		DATE_FORMAT("dd-MMM-yy");
		private String text;
		private Default(String text){ this.text = text; }
		public String getText() { return text; }
		public void setText(String text) { this.text = text; }
	};

	/**Method use for generate Ivr id*/
	default String generateId() {
		return "ivr-" + A3ServiceUtil.generateRandonAlphaNumberic(15, 15, "");
	}
	
	/**Method use for generate Irs id*/
	default Type getType() {
		return Type.IVR;
	}
	
	/**return list of mandatory {@link Dial} configuration fields that are necessary to start an ivr call*/
	public default String[] listMandatoryFieldsForPostingRequest(IrsTemplate irsTemplate) {
		int ivrFieldsLength = 6;
		int ivrTemplateOverrideLength = irsTemplate == null ? 0 : (irsTemplate.listAllOverride() == null ? 0 : irsTemplate.listAllOverride().size());
		
		String [] fields = new String[ivrFieldsLength + ivrTemplateOverrideLength];
		fields[0] = AuthKey.IRS_VENDOR_ID.getText();
		fields[1] = IvrRecord.FROM_PHONE.getText();
		fields[2] = IvrRecord.CALLBACK_URL.getText();
		fields[3] = IvrRecord.TO_PHONE.getText();
		fields[4] = IrsRecord.TEMPLATE.getText();
		fields[5] = AuthKey.IRS_CLIENT_ID.getText();
		if (ivrTemplateOverrideLength > 0) {
			List<String> listOverrides = irsTemplate.listAllOverride();
			for(String str: listOverrides)
				fields[ivrFieldsLength++] = str;
		}
		return fields;
	}
}
