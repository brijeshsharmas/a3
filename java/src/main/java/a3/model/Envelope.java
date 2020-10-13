/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.model;

import java.util.List;

public class Envelope {

	private String from;
	private List<String> toList;
	private String subject;
	private String txtBody;
	private String htmlBody;
	private boolean success = true;
	private String errCode, errMsg;
	
	public Envelope() { }
	public Envelope(String from, List<String> toList, String subject, String txtBody, String htmlBody) {
		this.from = from;
		this.toList = toList;
		this.subject = subject;
		this.txtBody = txtBody;
		this.htmlBody = htmlBody;
	}

	public String getFrom() {return from;}
	public String getSubject() { return subject; }
	public String getTxtBody() { return txtBody; }	
	public String getHtmlBody() { return htmlBody; }
	public List<String> getToList() { return toList; }
	public boolean isSuccess() { return success; }
	public String getErrCode() { return errCode; }
	public String getErrMsg() { return errMsg; }
	
	public void setFrom(String from) { this.from = from; }
	public void setToList(List<String> toList) { this.toList = toList;}
	public void setSubject(String subject) { this.subject = subject; }
	public void setTxtBody(String txtBody) { this.txtBody = txtBody; }
	public void setSuccess(boolean success) { this.success = success; }
	public void setErrCode(String errCode) { this.errCode = errCode; }
	public void setErrMsg(String errMsg) { this.errMsg = errMsg; }
	public void setHtmlBody(String htmlBody) { this.htmlBody = htmlBody; }

}
