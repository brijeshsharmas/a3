/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.model.db;

import java.math.BigDecimal;

import a3.model.Model;
import a3.model.nosql.NoSQLDataType;
import a3.model.nosql.NoSQLKeyType;
import a3.service.A3DBService;
import a3.service.A3NoSQLService;
import a3.service.A3ServiceException;

/**
 * @author <b>Brijesh Sharma</b><br>
 * This class provides various utility method for {@link Model} for {@link A3DBService}
 */
public class CloudDBUtil {
	
	/**This method validate {@link KeyElement} and throw error if incorrect*/
	public static void throwErrorIfNotValidPrimaryKey(KeyElement keyElement) {
		if (keyElement == null) throw new A3ServiceException(Operation.CREATE_KEY_ELEMENT.name(),
				KeyElement.class.getSimpleName() + " Is Null");
		
		if (!isValidPrimaryKeyype(keyElement.getDBStorageType(), keyElement.getPrimaryKeyType()))
			throw new A3ServiceException(Operation.CREATE_KEY_ELEMENT.name(), "Could Not Create " + KeyElement.class.getSimpleName() + 
					" instance due to invalid Primary Key Type " + keyElement.getPrimaryKeyType());
		
		if (keyElement.getDataType() == null) return;
		
		if (!isValidAttributeDataType(keyElement.getDBStorageType(), keyElement.getDataType()))
				throw new A3ServiceException(Operation.CREATE_KEY_ELEMENT.name(), "Could Not Create " + KeyElement.class.getSimpleName() + 
						" instance due to invalid Data Type " + keyElement.getDataType());
	}
	
	public static String getDBDataTypeThrowErrorIfNotSupported(DBStorageType dbStorageType, Object value) {
		switch (dbStorageType) {
		case NOSQL:
			if (value == null) return NoSQLDataType.NULL.getText();
			if (value instanceof String || value instanceof Character) return NoSQLDataType.String.getText();
			if (value instanceof BigDecimal || value instanceof Float || value instanceof Double || 
					value instanceof Long || value instanceof Integer)
				return NoSQLDataType.Number.getText();
			if (value instanceof Boolean) return NoSQLDataType.Boolean.getText();

		default:
			throw new A3ServiceException(Operation.CREATE_ATTRIBUTE_ELEMENT.name(),
					value == null ? null : value.getClass() + " Data Type is NOT supported by " + A3NoSQLService.class.getSimpleName());
		}
	}
	
	public static boolean isValidAttributeDataType(DBStorageType dbStorageType, String dataType) {
		switch (dbStorageType) {
		case NOSQL:
			NoSQLDataType [] elements =  NoSQLDataType.values();
			for (NoSQLDataType element: elements) {
				if (element.getText().equalsIgnoreCase(dataType)) return true;
			}
			return false;

		default:
			return false;
		}	
	}
	public static boolean isValidPrimaryKeyype(DBStorageType dbStorageType, String primaryKeyType) {
		switch (dbStorageType) {
		case NOSQL:
			NoSQLKeyType [] elements =  NoSQLKeyType.values();
			for (NoSQLKeyType element: elements) {
				if (element.getType().equalsIgnoreCase(primaryKeyType)) return true;
			}
			return false;

		default:
			return false;
		}	
	}
}
