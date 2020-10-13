/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.model.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <b>Brijesh Sharma</b><br>
 * This class represents collection of one or more {@link A3Response} objects. The class also providers various builders method to 
 * create {@link A3Responses} and {@link A3Response} object
 */
public class A3Responses {

	private List<A3Response> cloudResponseList = new ArrayList<A3Response>();
	
	/**-----------------------------------------------------Constructors------------------------------------------------*/
	public A3Responses() {}
	public A3Responses(List <A3Response> cloudResponseList) { this.cloudResponseList = cloudResponseList;}
	
	/**-----------------------------------------------------Builder Method------------------------------------------------*/
	
	/**-----------------------------------------------------Getter Method------------------------------------------------*/
	public List<A3Response> asList() {	return cloudResponseList;	}
	public Map<String, A3Response> asMap() {	
		Map<String, A3Response> mapRespones = new HashMap<String, A3Response>();
		for (A3Response response: asList())
			mapRespones.put(response.getId(), response);
		
		return mapRespones;	
	}
	
	public List<A3Response> getResponses(String responseId) {
		if (cloudResponseList == null) return null;
		
		List<A3Response> list = new ArrayList<A3Response>();
		for (A3Response entry: cloudResponseList) {
			if ( entry.getId().equals(responseId) )
				list.add(entry);
		}
		return list;
	}
	/**Return first matching response entry object {@link A3Response}  from {{@link #cloudResponseList}}*/
	public A3Response getResponse(String messageId) {
		if (cloudResponseList == null)
			return null;
		
		for (A3Response entry: cloudResponseList) {
			if ( entry.getId().equals(messageId) )
				return entry;
		}
		return null;
	}

	/**-----------------------------------------------------Add Method------------------------------------------------*/
	public void addResponse(A3Response response) { cloudResponseList.add(response); }
	public static A3Responses build(Object cloudResponseObject) { 
		A3Responses cloudResponses = new A3Responses();
		cloudResponses.addResponse(A3Response.buildCloudResponse(cloudResponseObject));
		return cloudResponses;
	}
	public static A3Responses build(A3Request cloudRequest, Object cloudResponseObject) { 
		A3Responses cloudResponses = new A3Responses();
		A3Response cloudResponse = new A3Response(cloudRequest);
		cloudResponse.setObject(cloudResponseObject);
		cloudResponses.addResponse(cloudResponse);
		return cloudResponses;
	}
	public static A3Responses build(A3Requests cloudRequests, String cloudResponseMessage, boolean processedStatus) { 
		A3Responses cloudResponses = new A3Responses();
		for (A3Request cloudRequest: cloudRequests.asList()) {
			A3Response cloudResponse = new A3Response(cloudRequest);
			cloudResponse.setMessage(cloudResponseMessage);
			cloudResponse.setProcessedStatus(processedStatus);
			cloudResponses.addResponse(cloudResponse);
		}
		return cloudResponses;
	}
}
