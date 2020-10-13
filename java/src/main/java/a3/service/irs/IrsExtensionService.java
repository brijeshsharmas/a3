/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.service.irs;


import java.util.Properties;

import a3.model.request.A3Request;
import a3.model.request.A3Response;
import a3.service.A3Service;

/**
 * @author <b>Brijesh Sharma</b><br>
 * This interface defines contract for an inbound and outbound extension required by Ivr to process Ivr call data. An Ivr extension could be type
 * file, which takes xls, csv file as as input, validate it against mandatory Ivr fields and store that into Ivr tables.
 * The interface also contain enums as below<br>
 * {@link Extension} - defines list fields for {@link IrsExtensionService}<br>
 * {@link Error} - defines validation errors must be supported by implementation class bes<br>
 */
public interface IrsExtensionService extends A3Service{

	public enum Extension {
		ID("input_id"),
		NAME("input_name"),
		
		VALIDATION_STATUS("validation_status"),
		VALIDATION_ERROR("validation_error"),
		VALIDATION_PASSED("passed"),
		VALIDATION_FAILED("failed"),
		
		INPUT_DELIMITER("input_delimiter"), 
		OUTPUT_DELIMITER("output_delimiter"), 
		HEADER_CONFIG_DELIMITER("="), 
		HEADER_CONIG("HEADER_CONIG"),
		
		CREATED_DATE_TIME("created_date_time");
		 
		private String text;
		private Extension(String text){ this.text = text; }
		public String getText() { return text; }
		public void setText(String text) { this.text = text; }
	};
	
	/**Initialize {@link IrsExtensionService} using argument <code>props</code>*/
	public void initialize(Properties props);
	
	/**Process {@link A3Request} <code>request</code> and apply {@link TemplateService} <code>ivrTemplateService</code>*/
	public A3Response process(A3Request request, TemplateService ivrTemplateService);
	
	/**Derived class can use this function to over {@link IrsExtensionService.Extension#getText()} */
	public String getProperty(Extension property);
}
