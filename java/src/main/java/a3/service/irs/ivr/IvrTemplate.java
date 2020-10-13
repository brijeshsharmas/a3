/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.service.irs.ivr;

import a3.model.request.A3Request;
import a3.service.A3ServiceUtil;
import a3.service.irs.IrsTemplate;

/**
 * @author <br>Brijesh Sharma</b><br>
 * This interface defines an IvrTemplate, which primarily consist of <br>
 * 1. {@link #buildSay(Object, A3Request)}: A mandatory message, which an {@link IvrService} can play<br>
 * 2. {@link #buildGather(Object, A3Request)}: A optional message, which an {@link IvrService} can play while gathering callee inputs<br>
 * 3. {@link #getNumGatherDigits()}: For optional gather message, how many digits shall an {@link IvrService} must capture from	 callee inputs<br>
 */
public interface IvrTemplate extends IrsTemplate {
	
	public static enum IvrSchema {
		FINISH_ON_KEY("finishOnKey"), TEXT_TO_SPEECH_ACTOR("textToSpeechActor");
	
		private String text;
		private IvrSchema(String text){ this.text = text; }
		public String getText() { return text; }
		public void setText(String text) { this.text = text; }
	};
	
	/**Returns String, which act as completion of gather input*/
	public String getFinishOnKey();
	
	/**Returns Text To Speech Actor name*/
	public String getTextToSpeechActor();
	
	/**Default interface to build an invalid response message*/
	default String buildInvalidResponseOptionMessage(Object response) {
		String suffix = " Please select one of the valid options";
		String prefix = "I am sorry. ";
		return response == null ? prefix + "This is NOT a valid option." + suffix : 
			prefix + "You Pressed " + A3ServiceUtil.split(response.toString(), ',') + ", which is NOT a valid option." + suffix;
	}
	
	/**Default interface to build an invalid response message*/
	default String buildInvalidResponseDataFormatMessage(Object response, String responseFormat) {
		String suffix = " Please enter valid " + responseFormat + " format";
		String prefix = "I am sorry. ";
		return response == null ? prefix + "This is NOT a valid " + responseFormat + " format." + suffix : 
			prefix + "You Pressed " + A3ServiceUtil.split(response.toString(), ',') + ", which is NOT a valid " + responseFormat + " format." + suffix;
	}
	
	/**This method returns a boolean status if a given object response is an indicator of a replay message or not*/
	default boolean doReplayMessage(Object response) {
		if (response == null) return false;
		response = response.toString().trim();
		if (response.equals("*"))
			return true;
		return false;
	}
	
}
