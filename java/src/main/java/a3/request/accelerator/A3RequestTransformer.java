/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.request.accelerator;

import java.util.List;
import java.util.Map;

import a3.model.request.A3Request;
import a3.model.request.A3Response;
import a3.service.A3Logger;

/**
 * @author Brijesh Sharma
 *
 */
public interface A3RequestTransformer {
	
	public static String name = A3RequestTransformer.class.getSimpleName();
	
	public A3Request transform(Object object);
	
	public Object transform(A3Response response);
	public Object transformResponse(List<A3Response> listCloudResponse);
	
	public void setLogger(A3Logger logger);
	
	default void printQueryMap(Map<?, ?> queryMap, A3Logger logger) {
		logger.printInfo("Printing Map [" + queryMap + "]", name);
		String instanceType = (queryMap != null ? queryMap.getClass().getName() : null);
		logger.printInfo("Query Param Instance Type Is [" + instanceType + "]", name);
		if (instanceType != null ) {
			String valueInstanceInfo = null;
			for(Map.Entry<?, ?> entry: queryMap.entrySet()) {
				Object key = entry.getKey();
				Object value = entry.getValue();
				String keyInstanceType = key == null ? null : key.getClass().getName();
				String valueInstanceType = value == null ? null : value.getClass().getName();
				if (valueInstanceInfo == null) {
					valueInstanceInfo = "[{Key=" + entry.getKey() + ",KeyInstance=" + keyInstanceType 
							+ ",Value=" + entry.getValue() + ",ValueInstance=" + valueInstanceType + "}";
				}
				else
					valueInstanceInfo = valueInstanceInfo + ",{Key=" + entry.getKey() + ",KeyInstance=" + keyInstanceType
					+ ",Value=" + entry.getValue() + ",ValueInstance=" + valueInstanceType + "}";
			}
			valueInstanceInfo = valueInstanceInfo == null ? null : valueInstanceInfo + "]";
			logger.printInfo("Query Param Values Instance Types Are\r" + valueInstanceInfo, name);
		}
	}
}
