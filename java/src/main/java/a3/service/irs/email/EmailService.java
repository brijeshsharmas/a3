/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.service.irs.email;

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
public interface EmailService extends IrsService {
	
	public enum EmailRecord{
		FROM_EMAIL("from_email"),
		TO_EMAIL("to_email"),
		WEBHOOK("webhook")
		;

		private String text;
		private EmailRecord(String text){ this.text = text; }
		public String getText() { return text; }
		public void setText(String text) { this.text = text; }
	};

	/**Method use for generate Ivr id*/
	default String generateId() {
		return "ier-" + A3ServiceUtil.generateRandonAlphaNumberic(15, 15, "");
	}
	
	/**Method use for generate Irs id*/
	default Type getType() {
		return Type.EMAIL;
	}
	
	/**return list of mandatory {@link Dial} configuration fields that are necessary to start an ivr call*/
	public default String[] listMandatoryFieldsForPostingRequest(IrsTemplate irsTemplate) {
		int emailFieldsLength = 3;
		int emailTemplateOverrideLength = irsTemplate == null ? 0 : (irsTemplate.listAllOverride() == null ? 0 : irsTemplate.listAllOverride().size());
		
		String [] fields = new String[emailFieldsLength + emailTemplateOverrideLength];
		fields[0] = EmailRecord.TO_EMAIL.getText();
		fields[1] = IrsRecord.TEMPLATE.getText();
		fields[2] = AuthKey.IRS_CLIENT_ID.getText();
		if (emailTemplateOverrideLength > 0) {
			List<String> listOverrides = irsTemplate.listAllOverride();
			for(String str: listOverrides)
				fields[emailFieldsLength++] = str;
		}
		return fields;
	}
	
	public default String[] listMandatoryFieldsForPreparingCallbackURL(IrsTemplate irsTemplate ) {
		int irsFieldsLength = 2;
		int irsTemplateOverrideLength = irsTemplate == null ? 0 : (irsTemplate.listAllOverride() == null ? 0 : irsTemplate.listAllOverride().size());
		
		String [] fields = new String[irsFieldsLength + irsTemplateOverrideLength];
		fields[0] = IrsRecord.TEMPLATE.getText();
		fields[1] = IrsRecord.ID.getText();
		if (irsTemplateOverrideLength > 0) {
			List<String> listOverrides = irsTemplate.listAllOverride();
			for(String str: listOverrides)
				fields[irsFieldsLength++] = str;
		}
		return fields;
	}
	
}
