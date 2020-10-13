/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.model.request;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import a3.service.A3ServiceUtil;

/**
 * @author <b>Brijesh Sharma</b><br>
 * This class represent a single Cloud Response, which can be exchanged between cloud services. The class extends {@link A3Request} class<br>>
 * 
 * <b>Data Model</b><br>
 *1. {@link #cloudRequest}: respective {@link A3Request} reference for which this class stores response information<br>
 *2. {@link #isProcessedSuccesfully}: Indicating if corresponding {@link #cloudRequest} was executed successfully or not. Caller can set 
 *and get request processing status as boolean value<br>
 *3. {@link #code}: Indicating response code. Caller can get and set these code categorizing response status<br>
 *4. {@link #message}: Indicating response message such as error description.<br>
 */
public class A3Response extends A3Request implements Serializable, Cloneable {

	private static final long serialVersionUID = 1933555256365823649L;
	private A3Request cloudRequest;
	private boolean isProcessedSuccesfully = true; //Default to true
	private String code = "NA"; 
	private String shortMessage = null;
	private String message = null;
	
	/**enum describing Cloud Response related keys*/
	public enum Key {
		ID("Id"),
		CODE("Code"),
		RESPONSE("Response"),
		REQUEST("Request"),
		SHORT_MSG("ShortMessage"), 
		MESSAGE("Message"), 
		SUCCESS("Success"),
		ERROR("Error"),
		OBJECT("Object"),
		PARAMS("Params"),
		STATUS("ProcessingStatus");
		private String text;

		private Key(String text){ this.text = text; }
		public String getText() { return text; }
		public void setText(String text) { this.text = text; }
	};

	@Override
	public A3Request wrapIntoCloudRequest() { return new A3Request(getObject()); }
	
	@Override
	public String toJsonString() {
		ObjectNode body = JsonNodeFactory.instance.objectNode();
		ObjectNode responseBody = A3ServiceUtil.buildJsonBody(this);
		body.set("Response", responseBody);
		if(cloudRequest != null)
			body.set("Request", A3ServiceUtil.buildJsonBody(cloudRequest));
		return body.toString();
	}
	
	/**----------------------Constructors--------------------------------------------------------------------------------**/
	public A3Response() { super(); }
	public A3Response(A3Request cloudRequest) { super(); this.cloudRequest = cloudRequest; }
	
	/**----------------------Builder Methods--------------------------------------------------------------------------------**/
	public static String getResponseJsonTagName() {
		return "Response";
	}
	/**
	 * Builds {@link A3Response} object using given {@link A3Request} reference<br>
	 * {@link #setId(String)} will be auto-populated<br>
	 * {@link #setProcessedStatus(boolean)} set to true*/
	public static A3Response buildCloudResponse(A3Request cloudRequest) {
		return buildCloudResponse(cloudRequest, null, null, null, true);
	}
	/**
	 * Builds {@link A3Response} object using given {@link Object}>. <br>
	 * {@link A3Response#setId(String)} will be auto-populated<br>
	 * {@link A3Response#setProcessedStatus(boolean)} set to true
	 **/
	public static A3Response buildCloudResponse(Object object) {
		return buildCloudResponse(null, null, null, object, true);
	}
	/**
	 * Builds {@link A3Response} object using given {@link Object} and {@link boolean} processing status. <br>
	 * {@link A3Response#setId(String)} will be auto-populated<br>
	 **/
	public static A3Response buildCloudResponse(Object object, boolean processingStatus) {
		return buildCloudResponse(null, null, null, object, processingStatus);
	}
	/**
	 * Builds {@link A3Response} object using given {@link Object} and {@link boolean} processing status. <br>
	 * {@link A3Response#setId(String)} will be auto-populated<br>
	 **/
	public static A3Response buildCloudResponse(A3Request request, Object object, boolean processingStatus) {
		return buildCloudResponse(request, null, null, object, processingStatus);
	}
	/**
	 * Builds {@link A3Response} object using given {@link Object} and {@link boolean} processing status. <br>
	 * {@link A3Response#setId(String)} will be auto-populated<br>
	 **/
	public static A3Response buildCloudResponse(A3Request cloudRequest, boolean processingStatus, String message) {
		return buildCloudResponse(cloudRequest, null, message, null, processingStatus);
	}
	/**Builds {@link A3Response} object using given {@link String} id and {@link Object} object. <br>
	 * {@link A3Response#setProcessedStatus(boolean)} set to true
	 **/
	public static A3Response buildCloudResponse(String id, Object object) {
		return buildCloudResponse(null, id, null, object, true);
	}
	public static A3Response buildCloudResponse(A3Request request, String code, String shortMsg, String message) {
		return new A3Response(request).withCode(code).withShortMessage(shortMsg).withMessage(message);
	}
	/**
	 * Builds {@link A3Response} object using given {@link A3Request}, {@link String} id, {@link Object} object,
	 * {@link String} message and {@link boolean} processingStatus. <br>
	 **/
	public static A3Response buildCloudResponse(A3Request cloudRequest, String id, String message, 
			Object object, boolean processingStatus) {
		A3Response cloudResponse = new A3Response();
		cloudResponse = (cloudRequest != null ? cloudResponse.withCloudRequest(cloudRequest) : cloudResponse); 
		cloudResponse = (id != null ? cloudResponse.withId(id) : cloudResponse); 
		cloudResponse = (object != null ? cloudResponse.withObject(object) : cloudResponse); 
		cloudResponse = (message != null ? cloudResponse.withMessage(message) : cloudResponse); 
		
		cloudResponse.setProcessedStatus(processingStatus);
		return cloudResponse;
	}
	public static A3Response buildErrorCloudResponse(A3Request cloudRequest, String code, String shortMessage, String message) {
		A3Response cloudResponse = new A3Response();
		cloudResponse = (cloudRequest != null ? cloudResponse.withCloudRequest(cloudRequest) : cloudResponse); 
		cloudResponse = (code != null ? cloudResponse.withCode(code) : cloudResponse); 
		cloudResponse = (shortMessage != null ? cloudResponse.withShortMessage(shortMessage) : cloudResponse); 
		cloudResponse = (message != null ? cloudResponse.withMessage(message) : cloudResponse); 
		cloudResponse.setProcessedStatus(false);
		return cloudResponse;
	}
	/**---------------------------------Static Builder Method ends here*/
	public String buildStatusMessage() {
		return "Processed: {" + processed() + "}\nStatus Code: {" + getShortMessage() + ", " + getCode() + "}\nMessage: {"+ getMessage() +"}";
	}
	public A3Response withCloudRequest(A3Request cloudRequest) {
		this.cloudRequest = cloudRequest;
		update();
		return this;
	}
	public A3Response withProcessedStatus(boolean status) {
		this.isProcessedSuccesfully = status;
		update();
		return this;
	}
	
	public A3Response withCode(String code) {
		this.code = code;
		update();
		return this;
	}
	public A3Response withShortMessage(String message) {
		this.shortMessage = message;
		update();
		return this;
	}
	public A3Response withMessage(String message) {
		this.message = message;
		update();
		return this;
	}
	
	/**---------------Overriding Builder Methods from {@link A3Request}*/
	public A3Response withId(String id) {
		super.withId(id);
		return this;	
	}
	public A3Response withObject(Object object) {
		super.withObject(object);
		return this;
	}
	public A3Response withNativeObject(Object nativ) {
		super.withNativeObject(nativ);
		return this;
	}
	public A3Response withKeyValue(String key, String value) {
		super.withKeyValue(key, value);
		return this;	
	}
	public A3Request withIdLength(int length) {
		super.withIdLength(length);
		return this;	
	}
	public A3Response withIdPrefix(String prefix) {
		super.withIdPrefix(prefix);
		return this;
	}
	public A3Response build() {
		super.build();
		return this;
	}
	
	/**----------------------Setter Methods--------------------------------------------------------------------------------**/
	public void setProcessedStatus(boolean status) {
		this.isProcessedSuccesfully = status;
		update();
	}
	public void setCloudRequest(A3Request request) { this.cloudRequest = request; }
	public void setCode(String responseCode) { this.code = responseCode; }
	public void setMessage(String responseMessage) { this.message = responseMessage; }
	public void setShortMessage(String shortMessage) { this.shortMessage = shortMessage; }
	
	/**----------------------Getter Methods--------------------------------------------------------------------------------**/
	public String getShortMessage() {return shortMessage;}
	public String getRequestId() {
		if (cloudRequest == null) return null;
		return cloudRequest.getId();
	}
	public Object getRequestObject() {
		if (cloudRequest == null) return null;
		return cloudRequest.getObject();	
	}
	public A3Request getCloudRequest() {
		return cloudRequest;	
	}
	public boolean processed() { return isProcessedSuccesfully; }
	public String getCode() { return code; }
	public String getMessage() { return message; }
	
	public String buildErrorMessage() { return "Code:" + code + ",ShortDescription:" + shortMessage + ",LongDescription:" + message;}
	public String toString() {
		String toString =  getClass().getSimpleName() + "{Id:" + getId() + "|Object:[" + getObject() + "]|" +
				(cloudRequest !=null ? cloudRequest.toString() : "")
			+ "|Created_Time:" + getCreatedTime() + "|Updated_Time:" + getUpdatedTime() + "|Status:" + processed();
		toString += processed() ? "" : "|ErrorCode:" + getCode() + "|ErrorCodeDesc:" + getMessage();
		toString += "|KeyValue[";
		for(Map.Entry<String, String> entry: keyValueMap.entrySet()) {
			if(suppressFields.contains(entry.getKey().toString())) 
				toString = toString + "|" + entry.getKey() + ":***************";
			else toString = toString + "|" + entry.getKey() + ":" + entry.getValue();
		}
		toString = toString + "]|NativeObject:[" + getNativeObject()+"]}";
		return toString;	
	}
}
