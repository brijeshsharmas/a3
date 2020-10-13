/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.service.irs;


import java.util.Map;

import a3.service.A3ObjectStorageService;
import a3.service.A3Service;
import a3.service.A3StorageService;
import a3.service.irs.email.EmailTemplate;
import a3.service.irs.ivr.IvrTemplate;

/**
 * @author <b>Brijesh Sharma</b><br>
 * This interface defines contract for an Ivr Template Service, which can load and build templates using template schema available in the {@link A3StorageService}<br>
 */
public interface TemplateService extends A3Service {
	
	public enum Key {
		CACHE_TEMPLATE("cache_template");
		private String text;

		private Key(String text){ this.text = text; }
		public String getText() { return text; }
		public void setText(String text) { this.text = text; }
	};
	
	public IrsTemplate getTemplate(String templateName, IrsService.Type type);
	
	public boolean doExists(String templateName, IrsService.Type type);
	
	public IvrTemplate getIvrTemplate(String templateName);
	
	public EmailTemplate getEmailTemplate(String templateName);
	
	public Map<String, IrsTemplate> asMap();
	
	public void setCacheEnabled(boolean cache);
	
	public void setStorageService(A3ObjectStorageService storageService);

}
