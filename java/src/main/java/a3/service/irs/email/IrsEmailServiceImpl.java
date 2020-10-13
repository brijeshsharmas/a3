/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.service.irs.email;

import java.util.Properties;

import a3.model.request.A3Request;
import a3.model.request.A3Response;
import a3.service.A3Logger;
import a3.service.irs.TemplateService;

public class IrsEmailServiceImpl implements EmailService {

	public IrsEmailServiceImpl() {
		// TODO Auto-generated constructor stub
	}

	public IrsEmailServiceImpl(Properties props) {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public A3Response getStatus(A3Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public A3Response process(A3Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object buildMessage(TemplateService templateService, A3Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLogger(A3Logger logger) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getCloudSRN() {
		// TODO Auto-generated method stub
		return null;
	}

}
