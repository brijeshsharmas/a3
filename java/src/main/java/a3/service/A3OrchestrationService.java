/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.service;

/**
 * @author Brijesh Sharma
 * This interface defines contract for AWS Orchestration Service
 */
public interface A3OrchestrationService extends A3Service {
	
	
	/**
	 * 
	 * @param input
	 * @return
	 */
	public String startExecution(String input);
	
	/**
	 * 
	 * @param input
	 * @param executionName
	 * @return
	 */
	public String startExecutionWithName(String input, String executionName);
	

}
