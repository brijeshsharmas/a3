/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.service.irs.email;

import a3.model.request.A3Request;
import a3.service.A3ServiceUtil;
import a3.service.irs.IrsTemplate;

/**
 * @author <br>Brijesh Sharma</b><br>
 * This interface defines an IvrTemplate, which primarily consist of <br>
 * 1. {@link #buildSay(Object, A3Request)}: A mandatory message, which an {@link EmailService} can play<br>
 * 2. {@link #buildGather(Object, A3Request)}: A optional message, which an {@link EmailService} can play while gathering callee inputs<br>
 * 3. {@link #getNumGatherDigits()}: For optional gather message, how many digits shall an {@link EmailService} must capture from	 callee inputs<br>
 */
public interface EmailTemplate extends IrsTemplate {
	
	public static enum EmailSchema {
		FROM_EMAIL("fromEmail"), SUBJECT("subject");
	
		private String text;
		private EmailSchema(String text){ this.text = text; }
		public String getText() { return text; }
		public void setText(String text) { this.text = text; }
	};
	
	/**Returns From email String*/
	public String getFromEmail();
	
	/**Build Email Subject based on the cloud request*/
	public String buildSubject(A3Request request);
	
	/**Return Email Subject Heading*/
	public String getSubject();
	
	/**Return Email Subject Heading*/
	public String getSignature();
	
	/**Default interface to build an invalid response message*/
	default String buildInvalidResponseOptionMessage(Object response) {
		String suffix = " Please select one of the valid options";
		String prefix = "I am sorry. ";
		return response == null ? prefix + "This is NOT a valid option." + suffix : 
			prefix + "You Pressed " + A3ServiceUtil.split(response.toString(), ',') + ", which is NOT a valid option." + suffix;
	}
}
