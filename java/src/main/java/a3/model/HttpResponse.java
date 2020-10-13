/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.model;

import static a3.service.A3Service.StatusCode.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import a3.model.request.A3Request;
import a3.model.request.A3Response;
import a3.service.A3ServiceUtil;
/**
 * @author <b>Brijesh Sharma</b><br>
 * The response that will be the passed back to the API Gateway.
 * <p>
 * The implementation depends on the AWS API Gateway response template and
 * is designed to get serialized to it.
 *
 *
 */
public final class HttpResponse implements Model {

	private final String body;
	private final String name = getClass().getSimpleName();
	private final Map<String, String> headers;
	private final int statusCode;
	private final boolean base64Encoded;

	public HttpResponse() {
		this.statusCode = 200;
		this.body="{}";
		this.headers = new HashMap<>();
		this.base64Encoded = false;
	}
	
	
	public HttpResponse(A3Response response, Map<String, String> headers, boolean base64Encoded) {
		this.statusCode = A3ServiceUtil.isNumeric(response.getCode()) ? Integer.parseInt(response.getCode()) : Integer.parseInt(UNKNOWN.code());
		this.headers = Collections.unmodifiableMap(new HashMap<>(headers));
		this.base64Encoded = base64Encoded;
		body = response == null ? "{}" : response.toJsonString();
	}

	public String getBody() {
		return body; 
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public int getStatusCode() {
		return statusCode;
	}
	// APIGW expects the property to be called "isBase64Encoded"
	public boolean isIsBase64Encoded() {
		return base64Encoded;
	}

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}
		if (other == null) {
			return false;
		}
		if (!getClass().equals(other.getClass())) {
			return false;
		}
		HttpResponse castOther = (HttpResponse) other;
		return Objects.equals(body, castOther.body)
				&& Objects.equals(headers, castOther.headers)
				&& Objects.equals(statusCode, castOther.statusCode)
				&& Objects.equals(base64Encoded, castOther.base64Encoded);
	}

	@Override
	public int hashCode() {
		return Objects.hash(body, headers, statusCode, base64Encoded);
	}

	@Override
	public String toString() {
		//return name + "[{\"body\":" + body + ", \"headers\":" + CloudServiceUtil.transformObjectToJsonString(headers) + ", \"statusCode\":" + statusCode + ", \"base64Encoded\":" + base64Encoded + "}]";
		return name + " [body=" + body + ", headers=" + headers + ", statusCode=" + statusCode
				+ ", base64Encoded=" + base64Encoded + "]";
	}


	@Override
	public A3Request wrapIntoCloudRequest() {
		return new A3Request(this);
	}


	@Override
	public String toJsonString() {
		return name + "[{\"body\":" + body + ", \"headers\":" + A3ServiceUtil.transformObjectToJsonString(headers) 
			+ ", \"statusCode\":" + statusCode + ", \"base64Encoded\":" + base64Encoded + "}]";
	}
}