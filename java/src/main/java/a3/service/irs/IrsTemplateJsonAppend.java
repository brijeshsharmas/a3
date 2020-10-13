/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.service.irs;

import java.util.Objects;

public class IrsTemplateJsonAppend {

	private String response;
	private String field;
	private Object value;
	private String message;
	private String name = getClass().getSimpleName();
	
	public IrsTemplateJsonAppend() {}
	public IrsTemplateJsonAppend(String response, String field, Object value, String message) {
		this.response = response;
		this.field = field;
		this.value = value;
		this.message = message;
	}

	public String getResponse() {	return response; }
	public void setResponse(String response) { this.response = response; }
	public String getField() { 	return field; }
	public void setField(String field) { this.field = field; }
	public String getMessage() { return message; }
	public void setMessage(String message) { this.message = message; }
	public Object getValue() { return value; }
	public void setValue(Object value) { this.value = value; }
	
	@Override
	public boolean equals(final Object other) {
		if (this == other)  return true;
		if (other == null) return false;
		if (!getClass().equals(other.getClass())) return false;
	
		IrsTemplateJsonAppend castOther = (IrsTemplateJsonAppend) other;
		return Objects.equals(response, castOther.response)
				&& Objects.equals(field, castOther.field)
				&& Objects.equals(value, castOther.value)
				&& Objects.equals(message, castOther.message);
	}
	
	@Override
	public int hashCode() { return Objects.hash(response, field, value, message); }
	
	@Override
	public String toString() {
		return name + " [response=" + response + ", field=" + field + ", value=" + value + ", message=" + message + "]";
	}

}
