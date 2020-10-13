/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.model.nosql;

public enum NoSQLDataType {

	String("S"), Binary("B"), Number("N"), Boolean("B"), Array("A"), Object("O"), NULL("NULL)");
	
	private String text;

	private NoSQLDataType(String text){ this.text = text; }
	public String getText() { return text; }
	public void setText(String text) { this.text = text; }
	
}
