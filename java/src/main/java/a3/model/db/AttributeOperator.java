/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.model.db;

public enum AttributeOperator {
	
	GT(">"), GT_EQ(">="), LT("<"), LT_EQ("<="), EQ("=") ;
	
	private String expression;
	private AttributeOperator(String expression){ this.expression = expression;  }
	public String getExpression() { return expression; }
	public void setExpression(String type) { this.expression = type; }
}
