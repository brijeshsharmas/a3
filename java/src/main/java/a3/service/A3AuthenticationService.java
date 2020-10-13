/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.service;

import a3.model.request.A3Request;
import a3.model.request.A3Response;

/**
 * @author Brijesh Sharma
 *
 */
public interface A3AuthenticationService extends A3Service{
	
	public enum Key {
		CREDENTIAL_STORE("credential_store"), USER_ID("user_id"), PASSWORD("password"), AUTH_TYPE("auth_type");
		private String text;

		private Key(String text){ this.text = text; }
		public String getText() { return text; }
		public void setText(String text) { this.text = text; }
	};
	
	public enum AuthType {
		USER_ID_ONLY("user_only"), USER_AND_PASSWORD("user_and_password");
		private String text;

		private AuthType(String text){ this.text = text; }
		public String getText() { return text; }
		public void setText(String text) { this.text = text; }
	};
	
	public A3Response authenticate(A3Request request);
	public A3Response getPassword(A3Request request);

}
