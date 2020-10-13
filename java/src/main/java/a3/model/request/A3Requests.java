/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.model.request;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <b>Brijesh Sharma</b><br>
 * This class represents collection of one or more {@link A3Request} objects
 *
 */
public class A3Requests {

	private List<A3Request> requests = new ArrayList<A3Request>();
	
	/**-----------------------------------------------------Constructors------------------------------------------------*/
	public A3Requests() {}
	public A3Requests(List <A3Request> requests) { this.requests = requests; }
	public A3Requests(String requestId, Object requestObject) {
		A3Request entry = new A3Request(requestId, requestObject);
		requests.add(entry);
	}
	public A3Requests(Object requestObject) {
		A3Request entry = new A3Request(requestObject);
		requests.add(entry);
	}
	
	/**-----------------------------------------------------Getter Method------------------------------------------------*/
	public List<A3Request> asList() {	return requests; }
	public List<A3Request> getRequests(String requestId) {	
		if (requests == null) return null;
		
		List<A3Request> listRequests= new ArrayList<A3Request>();
		for (A3Request entry: requests) {
			if ( entry.getId().equals(requestId) ) listRequests.add(entry);
		}
		
		return listRequests; 
	}
	/**Return first matching request entry object {@link A3Request}  from {{@link #requests}}*/
	public A3Request getRequest(String requestId) {
		if (requests == null) return null;
		
		for (A3Request entry: requests) {
			if ( entry.getId().equals(requestId) ) return entry;
		}
		
		return null;
	}
	
	/**-----------------------------------------------------Add Method------------------------------------------------*/
	public void addRequest(String requestId, Object requestObject) {
		A3Request entry = new A3Request(requestId, requestObject);
		requests.add(entry);
	}
	public void addRequest(Object requestObject) {
		A3Request entry = new A3Request(requestObject);
		requests.add(entry);
	}
	
}
