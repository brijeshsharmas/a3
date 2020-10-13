/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.service;

import java.util.Date;
import java.util.Map;
import java.util.Properties;

import java.io.PrintStream;
import java.text.SimpleDateFormat;

public class A3Logger {
	
	private PrintStream out = System.out;
	private Date date = new Date();
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private static final String STR_INFO = "INFO:";
	private static final String STR_ERR = "EROOR:";
	private static final String STR_WARN = "WARN:";
	private Properties props = null;
	private boolean boolLogInfo = true;

	public A3Logger() {	}
	public void setPrintStream(PrintStream stream) {out = stream;}

	public A3Logger(Map<String, String> map) {
		this.props = new Properties();
		for (Map.Entry<String, String> entry : map.entrySet()) 
			props.put(entry.getKey(), entry.getValue());
	}

	public A3Logger(Properties props) {this.props = props;}

	/**@deprecated use {@link #printInfo(Object, String)} instead */
	@Deprecated
	public void printInfo(Object infoMsg) {	out.println(STR_INFO + infoMsg);}
	public void printInfo(Object infoMsg, String callerName) {	
		date.setTime(System.currentTimeMillis());
		out.println("[" + dateFormat.format(date) + "][" + STR_INFO + "][" + callerName + "]. " + infoMsg);
	}
	/**@deprecated use {@link #printErr(Object, String)} instead */
	@Deprecated
	public void printErr(Object errMsg) {System.out.println(STR_ERR + errMsg);}
	public void printErr(Object errMsg, String callerName) {	
		date.setTime(System.currentTimeMillis());
		out.println(STR_ERR + "[" + callerName + "]. " + errMsg);
	}
	/**@deprecated use {@link #printWarn(Object, String)} instead */
	@Deprecated
	public void printWarn(Object warnMsg) {	System.out.println(STR_WARN + warnMsg);}
	public void printWarn(Object warnMsg, String callerName) {	printWarn(warnMsg, callerName, false);}
	public void printWarn(Object warnMsg, String callerName, boolean toUpperCase) {	
		if (toUpperCase && warnMsg != null)
			warnMsg = warnMsg.toString().toUpperCase();
		date.setTime(System.currentTimeMillis());
		out.println("[" + dateFormat.format(date) + "][" + STR_INFO + "][" + callerName + "]. " + warnMsg);
	}

	public boolean isLogInfo() {return boolLogInfo;}
	public void setLogInfo(boolean logInfo) {this.boolLogInfo = logInfo;}
	public void printBlankLine() {System.out.print("\r");}
}
