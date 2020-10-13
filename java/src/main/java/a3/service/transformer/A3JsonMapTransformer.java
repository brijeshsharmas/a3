/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.service.transformer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import a3.model.request.A3Request;
import a3.model.request.A3Response;
import a3.request.accelerator.A3RequestTransformer;
import a3.service.A3Logger;
import a3.service.A3ServiceException;
import a3.service.A3ServiceUtil;
import a3.service.A3AuthenticationService.Key;
import a3.service.A3RestAPIService.ContentType;

import static a3.model.request.A3Response.Key.REQUEST;
import static a3.model.request.A3Response.Key.RESPONSE;
import static a3.service.A3RestAPIService.Key.*;

/**
 * @author <b>Brijesh Sharma</b><br>
 * This class is an implementation of {@link A3RequestTransformer} interface for handling Json request. 
 */
public class A3JsonMapTransformer implements A3RequestTransformer {
	
	protected A3Logger logger = null;
	private String name = getClass().getSimpleName();
	private List<String> suppressedFields = Arrays.asList(Key.CREDENTIAL_STORE.getText(), Key.USER_ID.getText(), Key.PASSWORD.getText(), Key.AUTH_TYPE.getText());

	/** ---------------------CONSTRUCTORS--------------------------------**/
	public A3JsonMapTransformer() {	}
	public A3JsonMapTransformer(A3Logger logger) { this.logger = logger; }

	/** ----------------------Override CloudMessageTransformer-------------------------------**/
	@Override
	public A3Request transform(Object object) {
		throwErrorIfInputIsNotType_Map(object);
		return transformFromMapToCloudRequest(((Map<?, ?>)object));
	}
	
	@Override
	public Object transformResponse(List<A3Response> listCloudResponses) {
		return null;
	}
	@Override
	public Object transform(A3Response response) {
		
		ObjectNode root = JsonNodeFactory.instance.objectNode();
		ObjectNode body = JsonNodeFactory.instance.objectNode();
		root.set(BODY.getText(), body);
		
		if(A3ServiceUtil.isNumeric(response.getCode()))
			root.put(STATUS_CODE.getText(), Integer.parseInt(response.getCode()));
		else
			root.put(STATUS_CODE.getText(), response.getCode());
		
		Map<String, Object> mapHeaders = new HashMap<String, Object>();
		mapHeaders.put(CONTENT_TYPE.getText(), ContentType.JSON.getText());
		String jsonHeader =  A3ServiceUtil.transformObjectToJsonString(mapHeaders);
		root.set(HEADERS.getText(), A3ServiceUtil.transformObjectToJsonNode(jsonHeader));
		
		ObjectNode responseBody = A3ServiceUtil.buildJsonBody(response);
		body.set(RESPONSE.getText(), responseBody);
		if(response.getCloudRequest() != null)
			body.set(REQUEST.getText(), A3ServiceUtil.buildJsonBody(response.getCloudRequest()));
		
		return root.toString();
	
	}
	
	@Override
	public void setLogger(A3Logger logger) {	this.logger = logger;}
	
	/** ---------------------Transform AWS Lambda Event Object Into CloudMessageRequest------------------------------**/
	protected A3Request transformFromMapToCloudRequest(Map<?, ?> queryMap) {
		A3Request request = new A3Request();
		request.setSuppressFields(suppressedFields);
		if (queryMap == null) return request;
		
		int counter = 0;
		for (Map.Entry<?, ?> entry: queryMap.entrySet()) {
			if(entry == null || entry.getKey() == null || entry.getValue() == null) continue;
			
			if(entry.getValue() instanceof Map<?, ?>)
				populateCloudRequestWithQueryMap(request, ((Map<?, ?>)entry.getValue()), ++counter);
			else 
				request.addKeyValue(entry.getKey().toString(), entry.getValue().toString());	
		}
		return request;
	}
	protected void populateCloudRequestWithQueryMap(A3Request request, Map<?, ?> queryMap, int counter) {
		//Safety net to avoid infinite loop
		if (counter > 5) {
			 logger.printWarn("Recursive Counter To Populate Query Map Has Reached Max Limit Of 5, Aborting Any Further Population Of Query Map ", name);
			return;
		}
		
		for(Map.Entry<?, ?> entry: queryMap.entrySet()) {
			if(entry == null || entry.getKey() == null || entry.getValue() == null) continue;
			
			if (entry.getValue() instanceof Map<?, ?>) 
				populateCloudRequestWithQueryMap(request, ((Map<?, ?>)entry.getValue()), ++counter);
			else {
				String key = entry.getKey().toString();
				request.addKeyValue(key, entry.getValue().toString());
			}
		}
	}
	protected void throwErrorIfInputIsNotType_Map(Object object) {
		if(!(object instanceof Map<?, ?>)) {
			String errMsg = "Inbound Request Type [" + (object == null ? "null": object.getClass().getName()) + "] Is Not Supported By [" + name + "]";
			logger.printErr(errMsg, name);
			throw new A3ServiceException("Transform", errMsg);
		}
	}
	public A3Logger getLogger() { return logger; }
	public List<String> getSuppressedField() {return suppressedFields;}
}
