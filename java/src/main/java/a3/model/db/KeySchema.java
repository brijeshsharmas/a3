/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.model.db;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import a3.service.A3DBService;
import a3.service.A3ServiceUtil;

/**
 * @author <b>Brijesh Sharma</b><br>
 * This class represents key schema of a {@link A3DBService} services including Relational and Non-Relation database. The key schema contains information 
 * about each key such as key name, data type, key type i.e. primary/foreign/partition/sort key. 
 * Each stored key is a representation of {@link KeyElement}
 */
public class KeySchema implements Serializable{

	private static final long serialVersionUID = -9010068843981448374L;
	private Map<String, KeyElement> keySchema = new HashMap<String, KeyElement>(); 

	/**----------------------------------------------Constructors---------------------------**/
	public KeySchema() {}
	public KeySchema(DBStorageType dbStorageType, String name, String dataType, String primaryKeyType) {
		KeyElement keyElement = new KeyElement(dbStorageType, name, dataType, primaryKeyType);
		keySchema.put(keyElement.getName(), keyElement);
	}
	

	public void deleteKeyElement(String keyName) { 
		if(keyName == null) return;
		keySchema.remove(keyName);
	}
	
	/**----------------------------------------------Add/Setter Methods---------------------------**/
	public void addKey(KeyElement keyElement) { keySchema.put(keyElement.getName(), keyElement); }
	public void addKey(DBStorageType dbStorageType, String name, String dataType, String primaryKeyType) { 
		KeyElement keyElement = new KeyElement(dbStorageType, name, dataType, primaryKeyType);
		keySchema.put(keyElement.getName(), keyElement);
	}
	public void setKeySchema(Map<String, KeyElement> keySchema) { this.keySchema = keySchema; }
	
	/**----------------------------------------------Getter Methods---------------------------**/
	public Map<String, KeyElement> getKeySchema() {	return keySchema;}
	public KeyElement getKey(String keyName) { return keySchema.get(keyName); }
	
	public String toString( ) { return getClass().getSimpleName() +"[" + A3ServiceUtil.transformObjectToJsonString(keySchema) + "]";}
	
}
