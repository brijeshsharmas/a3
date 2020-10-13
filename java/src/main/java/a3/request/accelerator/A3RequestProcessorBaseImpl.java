/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.request.accelerator;

import java.util.Properties;

import a3.model.request.A3Request;
import a3.model.request.A3Response;
import a3.service.A3Logger;
import a3.service.A3Service;

/**
 * @author <b>Brijesh Sharma</b><br>
 * This is the base implementation class of {@link A3RequestProcessor} interface, which take cares of setting properties, payload and logger injection, hence
 * avoiding developer to override these methods for each implementation. Developers are advised to extend this class in lieu 
 * directly implementing {@link A3RequestProcessor} interface.
 */
public class A3RequestProcessorBaseImpl implements A3RequestProcessor {

	protected Properties props = null;
	protected A3Request request = null;
	protected A3Logger logger = null;
	private String name = getClass().getSimpleName();

	@Override
	public A3Response execute() {
		// Developer must override this method
		return null;
	}

	@Override
	public void setCloudServices(A3Service[] cloudServices) {
		// Developer must override this method
	}

	@Override
	public void setPayload(A3Request request) { this.request = request; }

	@Override
	public void setLogger(A3Logger logger) { this.logger = logger;}

	@Override
	public void setProperties(Properties props) { this.props = props;}

	@Override
	public void setName(String name) { this.name = name; }

	@Override
	public String getName() { return name;}
	
	public A3Request getRequest() { return request;}
	
	public A3Logger getLogger() { return logger; }
	
	public Properties getProperties() { return props; }
	
	public void printInfo(String infoMsg) { logger.printInfo(infoMsg, name);}
	public void printErr(String errMsg) { logger.printErr(errMsg, name);}
	public void printWarn(String warnMsg) { logger.printWarn(warnMsg, name);}

}
