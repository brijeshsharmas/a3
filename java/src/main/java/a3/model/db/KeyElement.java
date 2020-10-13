/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.model.db;

import java.io.Serializable;

import a3.model.nosql.NoSQLDataType;
import a3.model.nosql.NoSQLKeyType;
import a3.service.A3DBService;

/**
 * @author <b>Brijesh Sharma</b><br>
 * This class represents a single key element of a {@link A3DBService} service schema. The key element contains primary key name, data type, 
 * primary key type i.e. primary/foreign/partition/sort key. The class also ensure that only support primary key and data types are allowed while forming KeyElement. <br>
 * For a list of supported NoSQL primary key types, please refer to enum {@link NoSQLKeyType}
 * For a list of supported NoSQL data type, please refer to enum {@link NoSQLDataType}
 *
 */
public class KeyElement extends AttributeElement implements Serializable{

	private static final long serialVersionUID = -1192253386951485372L;
	private String primaryKeyType;
	
	/**----------------------------------------------Constructors---------------------------**/
	public KeyElement(DBStorageType dbStorageType, String name, String dataType, String primaryKeyType) {
		super(dbStorageType, name, dataType);
		this.primaryKeyType = primaryKeyType;
		CloudDBUtil.throwErrorIfNotValidPrimaryKey(this);
	}
	
	/**----------------------------------------------Getter Methods---------------------------**/
	public String getPrimaryKeyType() { return primaryKeyType; }
	
	/**----------------------------------------------Setter Methods---------------------------**/
	public void setDataType(String keyDataType) { 
		dataType = keyDataType; 
		CloudDBUtil.throwErrorIfNotValidPrimaryKey(this);
	}
	public void setPrimaryKeyType(String primaryKeyType) { 
		this.primaryKeyType = primaryKeyType; 
		CloudDBUtil.throwErrorIfNotValidPrimaryKey(this);
	}
	public void setName(String keyName) { this.name = keyName; }
	
	/**----------------------------------------------Helper Methods---------------------------**/
}
