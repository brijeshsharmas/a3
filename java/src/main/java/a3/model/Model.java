/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.model;

import a3.model.request.A3Request;

public interface Model {
	
	public A3Request wrapIntoCloudRequest();
	
	public String toJsonString();

}
