/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.service;

/**
 * @author brisharm0
 *
 */
public class A3ServiceException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8162434072700786411L;
	private String operation;
	
	/**
	 * 
	 * @param operation
	 * @param message
	 */
	public A3ServiceException(String operation, String message) {
		super(message);
		this.operation = operation;
	}
	
	
	public String getOperation() {
		
		return this.operation;
	}
	/**
	 * @param cause
	 */
	public A3ServiceException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public A3ServiceException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public A3ServiceException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}
	
	public String toString() {
		
		return operation + ":" + getMessage();
	}

}
