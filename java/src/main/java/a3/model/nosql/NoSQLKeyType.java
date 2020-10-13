/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.model.nosql;

public enum NoSQLKeyType {

	PARTITION_KEY("HASH"), SORT_KEY("RANGE");
	
	private String type;

	private NoSQLKeyType(String type){ this.type = type;  }
	public String getType() { return type; }
	public void setType(String type) { this.type = type; }
	
}
