/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.service;

/**
 * @author Brijesh Sharma
 * This interface defines contract required by implementation of all cloud storage services such as 
 * 1. OBJECT BASED STORAGE - Object based storage is a computer data storage architecture that manages data as objects, as opposed to other storage architectures like 
 * 		file systems which manages data as a file hierarchy, and block storage which manages data as blocks within sectors. A few example of Object Based Cloud Storage
 * 		services are S3 by AWS, BlobStorage by Microsoft Azure and CloudStorage by GCP
 * 2. BLOCK BASED STORAGE - Block storage is a category of data storage mostly used in storage area network (SAN) environments, where data is saved in huge volumes known 
 * 		as blocks. A few example of Block Based cloud storage services are EBS Volume by AWS, Disk Storage by Microsoft Azure  and Persistent Disk by GCP. 
 * 3. FILE BASED STORAGE - File storage, also called file-level or file-based storage, stores data in a hierarchical structure. The data is saved in files and folders, 
 * 		and presented to both the system storing it and the system retrieving it in the same format. A few example of File Based cloud storage services are EFS by AWS, 
 * 		File Storage by Microsoft Azure  and Firestore by GCP. 
 * 4. NON-RELATION OR NO-SQL DATABASE:
 * 5. RELEATIONAL DATABASE (RDBMS):
 * 6. KEY-VALUE CACHE STORES
 */
public interface A3StorageService extends A3Service {
	
	/**
	 * This function will clear all items inside the storage service instance. The caller needs to be aware that this operation could quite expensive for certain type
	 * of storage services like table as it will require complete table scan before emptying that table.
	 * @return
	 */
	public boolean clearAll();
	
	/**
	 * This function will delete storage service instance
	 * @return
	 */
	public boolean delete();
	

}
