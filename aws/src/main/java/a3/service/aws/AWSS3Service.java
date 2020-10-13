/**
 * 
 */
package a3.service.aws;

import static a3.service.aws.AWSServiceFactory.Service.*;
import static cloud.model.request.CloudResponse.*;
import static cloud.request.accelerator.CloudRequestAccelerator.Config.*;
import static cloud.service.CloudService.Property.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import cloud.model.objectstorage.ObjectStorageItem;
import cloud.model.request.CloudRequest;
import cloud.model.request.CloudRequests;
import cloud.model.request.CloudResponse;
import cloud.model.request.CloudResponses;
import cloud.service.CloudLogger;
import cloud.service.CloudObjectStorageService;
import cloud.service.CloudServiceException;
/**
 * @author Brijesh Sharma
 *
 */
public class AWSS3Service implements CloudObjectStorageService {
	
	private String strS3BucketName = null;
	private AmazonS3 awsS3Client = null;
	private String name = getClass().getSimpleName() ;
	private CloudLogger logger = null;
	private String serviceExceptionOperation = CLOUD_SERVICE_OPERATION.getText().
			replaceFirst(CLOUD_PROVIDER_SUBSITUTOR.getText(), "AWS").
			replaceFirst(SERVICE_NAME_SUBSITUTOR.getText(), S3.name());


	/** ------------------------------Constructor----------------------------------------**/
	public AWSS3Service(Properties props) { initialize(props); }
	
	/**---------------------{@link CloudObjectStorageService} method implementation----------------------------------*/
	
	/** Delete all objects from the bucket. This is sufficient for unversioned buckets. For versioned buckets, when you attempt 
	to delete objects, Amazon S3 inserts delete markers for all objects, but doesn't delete the object versions. To delete objects
	from versioned buckets, delete all of the object versions before deleting the bucket*/
	@Override
	public boolean clearAll() {
        ObjectListing objectListing = awsS3Client.listObjects(strS3BucketName);
        
        while (true) {
            Iterator<S3ObjectSummary> objIter = objectListing.getObjectSummaries().iterator();
            while (objIter.hasNext()) 
            	awsS3Client.deleteObject(strS3BucketName, objIter.next().getKey());

            /** If the bucket contains many objects, the listObjects() call might not return all of the objects in the first 
             * listing. Check to see whether the listing was truncated. If so, retrieve the next page of objects and delete them.*/
            if (objectListing.isTruncated()) objectListing = awsS3Client.listNextBatchOfObjects(objectListing);
            else break;
        }
        return true;
	}

	@Override
	public boolean delete() {
		if (clearAll())
			awsS3Client.deleteBucket(strS3BucketName);
		return true;
	}

	@Override
	public ByteBuffer readObject(String objectKey) {
		S3Object s3Object = awsS3Client.getObject(strS3BucketName, objectKey);
		InputStream objectData = s3Object.getObjectContent();
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	    int nRead;
	    try {
		    byte[] data = new byte[objectData.available()];
		    while ((nRead = objectData.read(data, 0, data.length)) != -1) {
		        buffer.write(data, 0, nRead);
		    }
		 
		    buffer.flush();
		    return ByteBuffer.wrap(buffer.toByteArray());
	    }catch (IOException exception) {
	    	String err = "Error Reading S3 Object " + objectKey + ". Exception " + exception.getMessage();
	    	logger.printErr(err, name);
	    	throw new CloudServiceException("S3ReadObject", err);
	    }
	}

	@Override
	public CloudResponse writeObject(ObjectStorageItem objectStorageItem) {
		//TO-DO: currently entire buffering happening in memory, which could cause out of memory error. This needs to be fixed
		awsS3Client.putObject(strS3BucketName, 
				objectStorageItem.getKey(),
					new ByteArrayInputStream(objectStorageItem.getBytes().array()),
						new ObjectMetadata());
		return buildCloudResponse(objectStorageItem, true);
	}

	@Override
	public CloudResponses writeObject(CloudRequests objectStorageItems) {
		CloudResponses responses = new CloudResponses();
		for (CloudRequest request: objectStorageItems.asList()) {
			responses.addResponse((writeObject((ObjectStorageItem)request.getObject())));
		}
		return responses;
	}
	
	@Override
	public void setLogger(CloudLogger logger) {	this.logger = logger;	}
	
	@Override
	public String getCloudSRN() { return strS3BucketName; }
	
	@Override
	public List<String> listAllKeys() {
		List<String> listKeys = new ArrayList<String>();
		ObjectListing objectListing = awsS3Client.listObjects(strS3BucketName);
        while (true) {
            Iterator<S3ObjectSummary> objIter = objectListing.getObjectSummaries().iterator();
            while (objIter.hasNext()) 
            	listKeys.add(objIter.next().getKey());

            /** If the bucket contains many objects, the listObjects() call might not return all of the objects in the first 
             * listing. Check to see whether the listing was truncated. If so, retrieve the next page of objects and delete them.*/
            if (objectListing.isTruncated()) objectListing = awsS3Client.listNextBatchOfObjects(objectListing);
            else break;
        }
        
        return listKeys;
	}

	@Override
	public boolean doExists(String objectKey) {
		List<String> keys = listAllKeys();
		return keys.contains(objectKey);
	}

	/**------------------Helper Methods Begins Here----------------------------------------------------------------------*/
	private void initialize(Properties props) {
		strS3BucketName = props.getProperty(SERVICE_ARNS.getText());
		if (strS3BucketName == null) throw new CloudServiceException(serviceExceptionOperation, 
				"Could not find service ARN in the property argument");
		awsS3Client =AmazonS3ClientBuilder.defaultClient();
	}

}
