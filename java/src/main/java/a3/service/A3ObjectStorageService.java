/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.service;

import java.nio.ByteBuffer;
import java.util.List;

import a3.model.objectstorage.ObjectStorageItem;
import a3.model.request.A3Requests;
import a3.model.request.A3Response;
import a3.model.request.A3Responses;

/**
 * @author Brijesh Sharma
 *
 */
public interface A3ObjectStorageService extends A3StorageService {
	
	public ByteBuffer readObject(String objectKey);
	
	public A3Responses writeObject(A3Requests objectStorageItems);
	
	public A3Response writeObject(ObjectStorageItem objectStorageItem);
	
	public List<String> listAllKeys();
	
	public boolean doExists(String objectKey);

}
