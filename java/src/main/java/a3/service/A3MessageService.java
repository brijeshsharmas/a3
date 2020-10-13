/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.service;
/**
 * @author Brijesh Sharma
 * This interface defines contract for publishing CloudServiceMessageRequest using cloud services
 */

import java.util.List;

import a3.model.message.CloudMessage;
import a3.model.request.A3Response;
import a3.model.request.A3Responses;

public interface A3MessageService extends A3Service {
	
	/**Publish given {@link CloudMessage} and return status in {@link A3Response} format*/
	public A3Response publishMessage(CloudMessage cloudMessage) ;
	
	/**Publish all {@link CloudMessage} and return their status in {@link A3Responses} format*/
	public A3Responses publishMessagesInBatch (List<CloudMessage> listCloudMessages) ;

}
