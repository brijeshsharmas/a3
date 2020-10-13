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
public interface A3RestAPIService extends A3Service {
	
	/**enum describing different context types*/
	public enum Key{
		ENDPOINT("endpoint"), REGION ("region"), HTTP_METHOD("http_method"), RESOURCE("resource"), BODY("body"), 
			HEADERS("headers"), CONTENT_TYPE("content_type"), HTTP_POST_METHOD("POST"), HTTP_GET_METHOD("GET"), STATUS_CODE("Status");
		private String text;
		private Key(String text){ this.text = text; }
		public String getText() { return text; }
		public void setText(String text) { this.text = text; }
	};
	
	/**enum describing different context types*/
	public enum ContentType {
		URL_ENCODED("application/x-www-form-urlencoded"), 
		JSON("application/json"),
		XML("application/xml");
		private String text;

		private ContentType(String text){ this.text = text; }
		public String getText() { return text; }
		public void setText(String text) { this.text = text; }
	};
	
	public A3Response execute(A3Request request);
	
	public default String[] listMandatoryKeyForExecution() {
		String [] listMandatoryKeys = new String[3];
		listMandatoryKeys[0] = Key.CONTENT_TYPE.getText();
		listMandatoryKeys[1] = Key.HTTP_METHOD.getText();
		listMandatoryKeys[2] = Key.RESOURCE.getText();
		return listMandatoryKeys;
	}

}
