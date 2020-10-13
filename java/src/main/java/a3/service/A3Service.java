/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.service;

/**
 * @author Brijesh Sharma
 * This is the top level marker interface representing all cloud services
 */
public interface A3Service  {
	public enum Property {
		CLOUD_PROVIDER_SUBSITUTOR("%%"), 
		SERVICE_NAME_SUBSITUTOR("~~"), 
		CLOUD_SERVICE_OPERATION("CLOUD SERVICE OPERATION: Creating %% ~~ Service"),
		DEFAULT_MSG_ID_LENGTH("30"),
		DEFAULT_MSG_ID_PREFIX("CS-MSGID-"),
		DEFAULT_MSG_ID_SEPARATOR_TOKEN_LENGTH("4"),
		DEFAULT_MSG_ID_SEPARATOR_TOKEN("-");
		private String text;

		private Property(String text){ this.text = text; }
		public String getText() { return text; }
		public void setText(String text) { this.text = text; }
	};
	
	/**enum describing cloud service execution status codes*/
	public enum StatusCode {
		UNKNOWN("-1"),
		SUCCESS("200"), 
		BAD_REQUEST("400"), 
		NOT_FOUND("404"),
		UNAUTHORIZED("401"),
		FORBIDDEN("403"),
		INTERNAL_ERROR("500"),
		UNAVAILABLE("503");
		private String text;

		private StatusCode(String text){ this.text = text; }
		public String code() { return text; }
		public void setText(String text) { this.text = text; }
	};
	
	/**
	 * 
	 * @param logger
	 */
	public void setLogger(A3Logger logger);
	
	/**
	 * Unique name of the Cloud Service Resource name
	 * @return
	 */
	public String getCloudSRN();
	
	

}
