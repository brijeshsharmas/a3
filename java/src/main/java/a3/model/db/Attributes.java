/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.model.db;

import java.util.HashMap;
import java.util.Map;

import a3.service.A3ServiceUtil;
/**
 * @author <b>Brijesh Sharma</b><br>
 * This class contains list column/attribute of type {@link AttributeElement} information of Cloud DB Storage services 
 * including Relational and Non-Relation database.  
 */
public class Attributes {
	
	private Map<String, AttributeElement> elements = new HashMap<String, AttributeElement>(); 

	/**----------------------------------------------Constructors---------------------------**/
	public Attributes() {}
	
	/**----------------------------------------------Builder Methods---------------------------**/
	public Attributes withAttributeElement(AttributeElement element) {
		elements.put(element.getName(), element);
		return this;
	}
	public void addElement(AttributeElement element) { elements.put(element.getName(), element); }
	
	/**----------------------------------------------Setter/Getter Methods---------------------------**/
	
	public void setAttributeElements(Map<String, AttributeElement> keySchema) { this.elements = keySchema; }
	
	public void deleteAttributeElement(String attrName) { 
		if(attrName == null) return;
		elements.remove(attrName);
	}
	
	public Map<String, AttributeElement> getAttributeElements() { return elements; }
	
	public String toString( ) {	return getClass().getSimpleName() +  "[" + A3ServiceUtil.transformObjectToJsonString(elements) + "]"; }
	
	public Object getAttributeValue(String attrName) {
		AttributeElement element =  elements.get(attrName);
		return element == null ? null : element.getDataValue();
	}
}
