/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.model.db;

import a3.model.nosql.NoSQLDataType;
import a3.model.nosql.NoSQLItem;
import a3.service.A3ServiceUtil;
/**
 * @author <b>Brijesh Sharma</b><br>
 * This class represents a single attribute in a {@link NoSQLItem}. An attribute element consist of the following <br>
 * {@link #name} - Mandatory attribute name. Attribute name must follow below type:<br>
 * a-z<br>
 * A-Z<br>
 * d0-9<br>
 * _ (underscore)<br>
 * - (dash)<br>
 * . (dot)<br>
 * {@link #dbStorageType}- Mandatory field. This field indicate for which DB storage (i.e. NoSQL, RDBMS, etc), does this element belongs. Depend upon the {@link #dbStorageType},
 * {@link #dataType} will be validated.
 * {@link #dataValue}  Optional Data Value<br>
 * {@link #dataType}. Optional Attribute data type. For NULL Data Value, {@link #dataType} will be {@link NoSQLDataType#NULL}.
 *  Supported NO SQL data types can be seen from {@link NoSQLDataType}<br>
 * {@link #conditionOerator} Optional Conditional Operator, which can use used for creating for NOSQL queries
 */
public class AttributeElement {
	protected String name, dataType;
	protected Object dataValue;
	protected DBStorageType dbStorageType;
	protected AttributeOperator conditionOerator = AttributeOperator.EQ;//Default Operator

	/**-------------------------------Constructors----------------------------------------------------------*/
	public AttributeElement(DBStorageType dbStorageType, String name) {
		this.dbStorageType = dbStorageType;
		this.name = name;
	}
	
	public AttributeElement(DBStorageType dbStorageType, String name, Object value) {
		this.dbStorageType = dbStorageType;
		this.setName(name);
		this.setDataValue(value);
	}
	public AttributeElement(DBStorageType dbStorageType, String name, Object value, AttributeOperator conditionOperator) {
		this.dbStorageType = dbStorageType;
		this.setConditionOperator(conditionOperator);
		this.setName(name);
		this.setDataValue(value);
	}
	protected AttributeElement(DBStorageType dbStorageType, String name, String dataType) {
		this.dbStorageType = dbStorageType;
		this.setName(name);
		this.dataType = dataType;
	}
	/**-------------------------------Builder Methods----------------------------------------------------------*/
	public AttributeElement withDataValue(Object dataValue) {
		this.setDataValue(dataValue);
		return this;
	}
	public AttributeElement withConditionOperator(AttributeOperator conditionOperator) {
		this.setConditionOperator(conditionOperator);
		return this;
	}

	/**-------------------------------Getter Methods----------------------------------------------------------*/
	public String getName() { return name; }
	public String getDataType() { return dataType; }
	public Object getDataValue() { return dataValue; }
	public AttributeOperator getConditionOerator() {return conditionOerator; }
	public DBStorageType getDBStorageType() {return dbStorageType;}
	
	/**-------------------------------Setter Methods----------------------------------------------------------*/
	public void setName(String name) {	this.name = name; }
	public void setConditionOperator(AttributeOperator conditionOerator) { this.conditionOerator = conditionOerator;}
	public void setDataValue(Object dataValue) { 
		this.dataValue = dataValue; 
		setDataType(dataValue);
	}
	private void setDataType(Object value) {
		this.dataType = CloudDBUtil.getDBDataTypeThrowErrorIfNotSupported(dbStorageType, value);
	}
	
	public String toString() { return A3ServiceUtil.transformObjectToJsonString(this); }
}
