/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.model.nosql;

import static a3.service.A3Service.Property.*;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import a3.model.db.AttributeElement;
import a3.model.db.AttributeOperator;
import a3.model.db.Attributes;
import a3.model.db.CloudDBUtil;
import a3.model.db.DBStorageType;
import a3.model.db.KeyElement;
import a3.model.db.KeySchema;
import a3.model.request.A3Response;
import a3.service.A3ServiceUtil;
/**
 * @author <b>Brijesh Sharma</b><br>
 * This class represents a single cloud Non-Relational or No-SQL DB storage item, which can be exchanged between cloud services.<br>
 * <b>Data Model</b><br> 
 * 1. {@link KeySchema} - contains item's primary key schema information such as name, data type, value and key type (Partition vs Sort key).
 * {@link KeyElement} is a representation of each key stored inside key schema.<br>
 * 2. {@link Attributes} - contains item's attribute information such as name, data type and value. {@link AttributeElement} is a 
 * representation of each attribute stored inside {@link Attributes} including {@link KeySchema}.<br>
 * 3. {@link #itemId} - Unique to each item, Caller can override this id.<br>
 * 4. {@link #createdTime} - Time when the item was created<br>
 * 5. {@link #updatedTime} - Time when the item was last updated<br>
 */ 
public class NoSQLItem implements Serializable, Cloneable {

	private static final long serialVersionUID = 4692685954484497609L;
	private KeySchema keySchema;
	private Attributes attributes;
	private String eventType;
	private Timestamp createdTime; 
	private Timestamp updatedTime; 
	private String itemId;
	
	/***------------------------------------Constructors-----------------------------*/
	public NoSQLItem() {
		this.createdTime = new Timestamp(System.currentTimeMillis());
		this.updatedTime = this.createdTime;
		setRequestDBStorageId(generateItemId());
	}

	/***------------------------------------Builder Method-----------------------------*/
	public static NoSQLItem clone(NoSQLItem item, List<String> listExcludeAttributes) {
		NoSQLItem newItem = new NoSQLItem();
		for(AttributeElement element: item.getAttributeElements().values()) {
			if (!listExcludeAttributes.contains(element.getName()))
				newItem.withAttribute(element);
		}
		KeyElement keyElement = item.getPartitionKey();
		if (keyElement != null && !listExcludeAttributes.contains(keyElement.getName()))
			newItem.withPrimaryKey(keyElement);
		keyElement = item.getSortKey();
		if (keyElement != null && !listExcludeAttributes.contains(keyElement.getName()))
			newItem.withPrimaryKey(keyElement);
		return newItem;
	}
	public A3Response buildCloudResponse() {
		return new A3Response().withObject(this);
	}
	public NoSQLItem withPrimaryKey(String partitionKey, Object partitionValue) {
		addPrimaryKey(partitionKey, partitionValue, NoSQLKeyType.PARTITION_KEY.getType());
		resetUpdatedTime();
		return this;
	}
	public NoSQLItem withPrimaryKey(KeyElement keyElement) {
		addPrimaryKey(keyElement.getName(), keyElement.getDataValue(), keyElement.getPrimaryKeyType());
		resetUpdatedTime();
		return this;
	}
	public NoSQLItem withSortKey(String sortKey, Object sortKeyValue) {
		addPrimaryKey(sortKey, sortKeyValue, NoSQLKeyType.SORT_KEY.getType());
		resetUpdatedTime();
		return this;
	}
	public NoSQLItem withPrimaryKeys(String partitionKey, Object partitionValue, String sortKey, Object sortKeyValue) {
		addPrimaryKey(partitionKey, partitionValue, NoSQLKeyType.PARTITION_KEY.getType());
		addPrimaryKey(sortKey, sortKeyValue, NoSQLKeyType.SORT_KEY.getType());
		resetUpdatedTime();
		return this;
	}
	private void addPrimaryKey(String key, Object value, String keyType) {
		if (keySchema == null) keySchema = new KeySchema();
		if (attributes == null) attributes = new Attributes();
		keySchema.addKey(new KeyElement(DBStorageType.NOSQL, key, 
				CloudDBUtil.getDBDataTypeThrowErrorIfNotSupported(DBStorageType.NOSQL, value),
				keyType));
		attributes.addElement(new AttributeElement(DBStorageType.NOSQL,
				key, value));
	}
	public NoSQLItem withAttribute(AttributeElement attrDefinition) {
		if (attributes == null) attributes = new Attributes(); 
		attributes.addElement(attrDefinition);
		resetUpdatedTime();
		return this;
	}
	public NoSQLItem withAttribute(String name, Object value) {
		withAttribute(new AttributeElement(DBStorageType.NOSQL,
				name, value));
		resetUpdatedTime();
		return this;
	}
	public NoSQLItem withAttribute(String name, Object value, AttributeOperator conditionOperator) {
		withAttribute(new AttributeElement(DBStorageType.NOSQL, name, value, conditionOperator));
		resetUpdatedTime();
		return this;
	}
	public NoSQLItem withKeySchema(KeySchema keySchema) {
		this.setKeySchema(keySchema);
		resetUpdatedTime();
		return this;
		
	}
	public NoSQLItem withAttributes(Attributes attributes) {
		this.setAttributes(attributes);
		resetUpdatedTime();
		return this;
	}
	
	/********-----------------------Remove Method-------------------------*/
	public void deleteAttribute(String attrName) {
		if (attrName == null) return;
		if (attributes != null ) attributes.deleteAttributeElement(attrName);
		if (keySchema != null ) keySchema.deleteKeyElement(attrName);
	}
	
	/********-----------------------Remove Method-------------------------*/
	/*---------------------------------Getter Method------------------------------------------*/
	public String getEventType() {return eventType;}
	public KeyElement getPartitionKey() { return getPrimaryKeyByType(NoSQLKeyType.PARTITION_KEY.getType()); }
	public Object getAttributeValue(String attributeName) { return getAttributes().getAttributeValue(attributeName); }
	public Object getAttributeValueIgnoreCase(String attributeName) { 
		for (AttributeElement element: getAttributeElements().values()) {
			if (element.getName().equalsIgnoreCase(attributeName))
				return element.getDataValue(); 
		}
		return null;
	}
	
	public KeyElement getSortKey() { return getPrimaryKeyByType(NoSQLKeyType.SORT_KEY.getType()); }
	private KeyElement getPrimaryKeyByType(String keyType) {
		if (keySchema == null) {
			keySchema = new KeySchema();
			return null;
		}
		if (keyType == null) return null;
		for(Map.Entry<String, KeyElement> keyElement: keySchema.getKeySchema().entrySet()) {
			if (keyType.equalsIgnoreCase(keyElement.getValue().getPrimaryKeyType()))
					return keyElement.getValue();
		}
		return null;
	}
	public Timestamp getUpdatedTime() {	return updatedTime;	}
	public Timestamp getCreatedTime() {	return createdTime;	}
	public KeySchema getKeySchema() { return keySchema;}
	public String getNoSQLDBStorageRequestId() { return itemId; }
	public Attributes getAttributes() { 
		if (attributes == null) attributes = new Attributes(); 
		return attributes;
	}
	public Map<String, AttributeElement> getAttributeElements() { return getAttributes().getAttributeElements(); }
	
	/********-----------------------Setter Method-------------------------*/
	public void setEventType(String eventType) {this.eventType = eventType; }
	public void setKeySchema(KeySchema keySchema) { this.keySchema = keySchema; 
		resetUpdatedTime();}
	public void setAttributes(Attributes attributes) { this.attributes = attributes;
		resetUpdatedTime();}
	public void setRequestDBStorageId(String requestDBStorageId) {this.itemId = requestDBStorageId;	
		resetUpdatedTime();}
	
	public String toString() {
		return (keySchema == null ? "KeySchema=Null, " : keySchema.toString()+", ") + (attributes == null ? "Attributes=Null" : attributes.toString());
	}
	private String generateItemId() {	
		return DEFAULT_MSG_ID_PREFIX.getText() + 
				A3ServiceUtil.generateRandonAlphaNumberic(
						Integer.parseInt(DEFAULT_MSG_ID_LENGTH.getText()) - DEFAULT_MSG_ID_PREFIX.getText().length() <= 0 ? 
								Integer.parseInt(DEFAULT_MSG_ID_LENGTH.getText()): 
									Integer.parseInt(DEFAULT_MSG_ID_LENGTH.getText()) - DEFAULT_MSG_ID_PREFIX.getText().length(),
										Integer.parseInt(DEFAULT_MSG_ID_SEPARATOR_TOKEN_LENGTH.getText()), DEFAULT_MSG_ID_SEPARATOR_TOKEN.getText());
	}
	private void resetUpdatedTime() { this.updatedTime = new Timestamp(System.currentTimeMillis());}
	
	/********-----------------------Helper Method-------------------------*/
	/**for each attribute in item, if attribute name and value matches, then return*/
	public boolean matchWith(NoSQLItem item) {
		if(item == null) return false;
		
		for(Map.Entry<String, AttributeElement> entry: getAttributeElements().entrySet()) {
			if(entry.getValue().getDataValue() == null) continue; //Ignore null value from match
			Object objValue = item.getAttributeValueIgnoreCase(entry.getKey());
			if(objValue == null) return false;
			if(!objValue.equals(entry.getValue().getDataValue())) return false;
		}
		return true;
	}
	

}
