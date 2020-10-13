/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.service.irs;


import static a3.service.irs.IrsService.Type.EMAIL;
import static a3.service.irs.IrsService.Type.IVR;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import a3.service.A3Logger;
import a3.service.A3ObjectStorageService;
import a3.service.A3ServiceUtil;
import a3.service.irs.IrsService.Type;
import a3.service.irs.email.EmailTemplate;
import a3.service.irs.email.EmailTemplateJsonImpl;
import a3.service.irs.ivr.IvrTemplate;
import a3.service.irs.ivr.IvrTemplateJsonImpl;
/**
 * @author Brijesh Sharma
 *
 */
public class IrsTemplateJsonService implements TemplateService {

	private String name = getClass().getSimpleName();
	
	private Map<String, IvrTemplate> mapIvrTemplates = new HashMap<String, IvrTemplate>();
	private Map<String, EmailTemplate> mapEmailTemplates = new HashMap<String, EmailTemplate>();
	
	private A3Logger logger = null;
	private A3ObjectStorageService objStorageService = null;
	
	private boolean cache = true;
	private String ivrTemplateFolderName = "templates" + File.separator + IVR.getText() + File.separator;
	private String emailTemplateFolderName = "templates" + File.separator + EMAIL.getText() + File.separator;
	
	public IrsTemplateJsonService(Properties props) {}

	@Override
	public void setLogger(A3Logger logger) { this.logger = logger;	}


	@Override
	public String getCloudSRN() {return name;}


	@Override
	public Map<String, IrsTemplate> asMap() { 
		Map<String, IrsTemplate> mapTemplates = new HashMap<String, IrsTemplate>();
		mapTemplates.putAll(mapIvrTemplates);
		mapTemplates.putAll(mapEmailTemplates);
		return mapTemplates;
	}

	@Override
	public void setCacheEnabled(boolean cache) {this.cache = cache;}

	@Override
	public void setStorageService(A3ObjectStorageService storageService) {
		objStorageService = storageService;
	}

	@Override
	public IvrTemplate getIvrTemplate(String templateName) {
		if(templateName == null) return null;
		
		if (cache && mapIvrTemplates.containsKey(templateName.trim().toLowerCase()) )
			return mapIvrTemplates.get(templateName);
		
		return loadIvrTemplate(templateName);
	}

	@Override
	public EmailTemplate getEmailTemplate(String templateName) {
		if(templateName == null) return null;
		
		if (cache && mapEmailTemplates.containsKey(templateName.trim().toLowerCase()) )
			return mapEmailTemplates.get(templateName);
		
		return loadEmailTemplate(templateName);
	}
	
	@Override
	public IrsTemplate getTemplate(String templateName, Type type) {
		if(templateName == null) return null;
		
		switch(type) {
		
		case IVR:
			if (cache && mapIvrTemplates.containsKey(templateName.trim().toLowerCase()) )
				return mapIvrTemplates.get(templateName);
			return getIvrTemplate(templateName);
		
		case EMAIL:
			if (cache && mapEmailTemplates.containsKey(templateName.trim().toLowerCase()) )
				return mapEmailTemplates.get(templateName);
			return getEmailTemplate(templateName);

		default: 
			logger.printErr("Could Not Load Template [" + templateName + "] Due To Invalid Template Type Argument [" + type + "]", name);
			return null;
		}
	}
	
	@Override
	public boolean doExists(String templateName, Type type) {
		if (templateName == null || type == null) return false;
		String templateFileName = null;
		switch (type) {
		case IVR:
			templateFileName = ivrTemplateFolderName + templateName + ".json";
			break;
		case EMAIL:
			templateFileName = emailTemplateFolderName+ templateName + ".json";
			break;
		default:
			break;
		}
		
		return objStorageService.doExists(templateFileName);
	}

	/**-------------------------------------Helper Methods------------------------*/
	private IvrTemplate loadIvrTemplate(String templateName) {
		String templateFileName = ivrTemplateFolderName + templateName + ".json";
		logger.printInfo("Loading Ivr Template File [" + templateFileName + "]", name);
		if (!objStorageService.doExists(templateFileName)) {
			logger.printInfo("Ivr Template File [" + templateFileName + "] Do Not Exists", name);
			return null;
		}
		IvrTemplate ivrTemplate = null;
		try {
			ByteBuffer buffer = objStorageService.readObject(templateFileName);
			ivrTemplate = A3ServiceUtil.transformJsonStringToObject(buffer.array(), IvrTemplateJsonImpl.class);
			ivrTemplate.setLogger(logger);
			if (cache)  mapIvrTemplates.put(ivrTemplate.getTemplateName().trim().toLowerCase(), ivrTemplate);
			logger.printInfo("Succesfully Loaded Ivr Template [" + ivrTemplate.getTemplateName() + "]", name);
		}catch(Exception e) {
			logger.printErr("Error Creating Ivr Template From File [" + templateFileName + "]. Error [" + e.getMessage() + "]", name);
			e.printStackTrace();
		}
		return ivrTemplate;
	}
	
	private EmailTemplate loadEmailTemplate(String templateName) {
		String templateFileName = emailTemplateFolderName + templateName + ".json";
		logger.printInfo("Loading Email Template File [" + templateFileName + "]", name);
		if (!objStorageService.doExists(templateFileName)) {
			logger.printInfo("Email Template File [" + templateFileName + "] Do Not Exists", name);
			return null;
		}
		EmailTemplate emailTemplate = null;
		try {
			ByteBuffer buffer = objStorageService.readObject(templateFileName);
			emailTemplate = A3ServiceUtil.transformJsonStringToObject(buffer.array(), EmailTemplateJsonImpl.class);
			emailTemplate.setLogger(logger);
			if (cache)  mapEmailTemplates.put(emailTemplate.getTemplateName().trim().toLowerCase(), emailTemplate);
			logger.printInfo("Succesfully Loaded Email Template [" + emailTemplate.getTemplateName() + "]", name);
		}catch(Exception e) {
			logger.printErr("Error Creating Email Template From File [" + templateFileName + "]. Error [" + e.getMessage() + "]", name);
			e.printStackTrace();
		}
		return emailTemplate;
	}

}
