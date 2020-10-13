/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.model.objectstorage;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * @author <b>Brijesh Sharma</b></br>
 * This class represent a single object storage entry, which can be exchanged between cloud object storage services 
 * such AWS S3, Azure CloudStorage, etc. 
 */

public class ObjectStorageItem implements Serializable, Cloneable {

	
	private static final long serialVersionUID = 4692685954484497609L;
	private String key;
	private ByteBuffer bytes;
	
	public String toString() {
		
		String itemString = "Key:" + getKey() + "|Object Size:" + getBytes().array().length;
		return itemString;
	}

	/**-----------------------------------------------------------Constructors--------------------------------*/
	public ObjectStorageItem() { super(); }
	public ObjectStorageItem(ByteBuffer byteBuffer) { this.bytes = byteBuffer; }
	public ObjectStorageItem(String key, ByteBuffer byteBuffer) {
		this.key = key;
		bytes = byteBuffer;
	}

	/**-----------------------------------------------------------Builder Method--------------------------------*/
	public ObjectStorageItem withKey(String key) { 
		this.key = key;
		return this;
	}
	public ObjectStorageItem withData(ByteBuffer byteBuffer) { 
		this.bytes = byteBuffer; 
		return this;
	}
	
	/**-----------------------------------------------------------Getter Method--------------------------------*/
	public ByteBuffer getBytes() { return bytes;	}
	public String getKey() {return key;}
	
}
