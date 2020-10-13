/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.model.message;

import static a3.service.A3Service.Property.*;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import a3.model.Model;
import a3.model.request.A3Request;
import a3.service.A3ServiceUtil;

/**
 * @author <b>Brijesh Sharma</b><br>
 * This class represent a single message entry, which can be exchanged between cloud services.<br>
 * 
 * <b>Data Model</b><br>
 * 1. {@link #msgId}: Uniquely identify a message. Caller can either set it as per their need, or use 
 * auto-generated by this class. There can be maximum of one message id per entry class<br>
 * 2. {@link #msgBody}: Message body<br>
 * 3. {@link #msgSubject}: Message body<br>
 * 4. {@link #msgHeaders}: Message headers in form of key value pair<br>
 * 5. {@link #createdTime} - Time when the item was created<br>
 * 6. {@link #updatedTime} - Time when the item was last updated<br>
 * 7. {@link #keyValue}: The class maintains an internal map of type <String, String>, which caller can use to store 
 * and get additional key-value pairs. For example, implementation of {@link A3RequestTransformer} must ensure to store cloud notification
 * service subject as a key with name {@link CloudServiceConstants#SUBJECT}<br>
 * 8. {@link nativeRequestObj}" implementation of {@link A3RequestTransformer} can use this store cloud provider
 * specific could native request object, which was transformed by {@link A3RequestTransformer}<br>
 */
public class CloudMessage implements Model, Serializable, Cloneable {

	public enum Key {MD5_HASH, SUBJECT, SEQUENCE_NUMBER, SHARD_ID};
	
	private static final long serialVersionUID = 4692685954484497609L;
	private String msgId;
	private String msgBody;	
	private String msgSubject;
	private Map<String, String> msgHeaders = new HashMap<String, String>();
	private Map<String, String> keyValue = new HashMap<String, String>();
	private Object nativeRequestObj = null;
	
	private String createdTime; //Time when the message was created
	private String updatedTime; //Time when the message was last updated

	/**----------------------{@link Model} Implementations------------------------**/
	@Override
	public A3Request wrapIntoCloudRequest() {
		return new A3Request(this);
	}
	
	/**----------------------Constructors------------------------**/
	public CloudMessage() {
		generateAndAssignMessageId();
		this.createdTime = new Timestamp(System.currentTimeMillis()).toString();
		update();
	}
	public CloudMessage(String msgBody) {
		generateAndAssignMessageId();
		this.msgBody = msgBody;
		this.createdTime = new Timestamp(System.currentTimeMillis()).toString();
		update();
	}
	public CloudMessage(String msgId, String msgBody) {
		this.msgId = msgId;
		this.msgBody = msgBody;
		this.createdTime = new Timestamp(System.currentTimeMillis()).toString();
		update();
	}
	public CloudMessage(String msgId, String msgBody, String msgSubject) {
		this.msgId = msgId;
		this.msgBody = msgBody;
		this.setSubject(msgSubject);
		this.createdTime = new Timestamp(System.currentTimeMillis()).toString();
		update();
	}
	/**-----------------------------Builder Methods-----------------------*/
	public CloudMessage withId(String msgId) {
		this.msgId = msgId;
		update();
		return this;	
	}
	public CloudMessage withBody(String msgBody) {
		this.msgBody = msgBody;
		update();
		return this;
	}
	public CloudMessage withSubject(String subject) {
		this.msgSubject = subject;
		update();
		return this;
	}
	public CloudMessage withNativeObject(Object nativ) {
		this.nativeRequestObj = nativ;
		update();
		return this;
	}
	public CloudMessage withKeyValue(String key, String value) {
		keyValue.put(key, value);
		update();
		return this;	
	}
	
	/**---------------------------------------Setter Methods----------------------------------*/
	public void setBody(String msgBody) {this.msgBody = msgBody; update(); }
	public void addKeyValue(String key, String value) { keyValue.put(key, value); update(); }
	public void setNativeObject(Object nativ) {	this.nativeRequestObj = nativ; update(); }
	public void setSubject(String msgSubject) { this.msgSubject = msgSubject; }
	public void setHeaders(Map<String, String> msgHeaders) {this.msgHeaders = msgHeaders; }
	
	/**---------------------------------------Getter Methods----------------------------------*/
	public String getId() { return msgId; }
	public String getBody() { return msgBody; }
	public Object getNativeObject() { return nativeRequestObj;}
	public String getValueByKey(String key) { return keyValue.get(key);	}
	public Map<String, String> getKeyValueMap() { return keyValue; }
	public String getUpdatedTime() {	return updatedTime;	}
	public String getCreatedTime() {	return createdTime;	}
	public String getSubject() { return msgSubject; }
	public Map<String, String> getHeaders() { return msgHeaders; }
	
	public String toString() {
		String requestString = getClass().getSimpleName() + "{MsgId:" + getId() + "|Message:[Body=\"" + 
				getBody() + "\",Subject=\"" + getSubject() + "\"]|Created_Time:" + getCreatedTime()
					+ "|Updated_Time:" + getUpdatedTime() + "|KeyValue:["; 
		for(Map.Entry<String, String> entry: keyValue.entrySet()) 
			requestString = requestString + "|" + entry.getKey() + ":" + entry.getValue();
		requestString = requestString + "]|Headers:[";
		for(Map.Entry<String, String> entry: msgHeaders.entrySet()) 
			requestString = requestString + "|" + entry.getKey() + ":" + entry.getValue();
		requestString = requestString + "]|NativeObject:[" + getNativeObject() + "]}";
		return requestString;
	}
	private String generateAndAssignMessageId() {
		return msgId = DEFAULT_MSG_ID_PREFIX.getText()  
				+ A3ServiceUtil.generateRandonAlphaNumberic(Integer.parseInt(DEFAULT_MSG_ID_LENGTH.getText()), 
						Integer.parseInt(DEFAULT_MSG_ID_SEPARATOR_TOKEN_LENGTH.getText()), DEFAULT_MSG_ID_SEPARATOR_TOKEN.getText());
	}
	private void update() {	this.updatedTime = new Timestamp(System.currentTimeMillis()).toString(); }

	@Override
	public String toJsonString() {
		return A3ServiceUtil.transformObjectToJsonString(this);
	}
}
