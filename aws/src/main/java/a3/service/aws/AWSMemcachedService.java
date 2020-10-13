package a3.service.aws;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Properties;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.elasticache.AmazonElastiCache;
import com.amazonaws.services.elasticache.AmazonElastiCacheClientBuilder;

import cloud.service.CloudCacheService;
import cloud.service.CloudLogger;
import cloud.service.CloudServiceException;
import net.spy.memcached.MemcachedClient;

import static a3.service.aws.AWSServiceFactory.Service.*;
import static cloud.request.accelerator.CloudRequestAccelerator.Config.*;
import static cloud.service.CloudService.Property.*;

/**
 * @author brisharm0
 *
 */
public class AWSMemcachedService implements CloudCacheService {

	private String strAWSCacheARN = null;
	private String signingRegion = null;
	private AmazonElastiCache client = null;
	private MemcachedClient netSpyClient = null;
	private int clusterPort = 11211;
	private CloudLogger logger = null;
	private final int defaultExpire = 3600;
	private String serviceExceptionOperation = CLOUD_SERVICE_OPERATION.getText().
			replaceFirst(CLOUD_PROVIDER_SUBSITUTOR.getText(), "AWS").
			replaceFirst(SERVICE_NAME_SUBSITUTOR.getText(), MEMCACHED.name());
	
	/**
	 * 
	 */
	public AWSMemcachedService(Properties props) throws CloudServiceException {
		
		strAWSCacheARN = props.getProperty(SERVICE_ARNS.getText());
		if (strAWSCacheARN == null) throw new CloudServiceException(serviceExceptionOperation, "Could not find service ARN in the property argument");
		
		signingRegion = props.getProperty("");
		//if (signingRegion == null) throw new AWSInfraServicesException(AWSServiceConstants.AWS_CACHE_SERVICE_OPERATION, "Could not find service Signing Region in the property argument");
		
		EndpointConfiguration config = new EndpointConfiguration(strAWSCacheARN, signingRegion);
		client = AmazonElastiCacheClientBuilder.standard().withEndpointConfiguration(config).defaultClient();
		
		try {
			System.out.println("Memcache EndPoint is " + strAWSCacheARN);
			netSpyClient = new MemcachedClient(new InetSocketAddress[] { new InetSocketAddress(strAWSCacheARN, 
				        clusterPort) });
		}catch (IOException exception) {
			System.out.println("Exception creating memcache client " + exception);
			exception.printStackTrace();
		}
		
				
	}
	
	
	public void set(String key, String value) {
		netSpyClient.set(key, defaultExpire, value);
		
	}
	
	public Object get(String key) {
		
		return netSpyClient.get(key);
	}
	
	public AmazonElastiCache getCache() {
		
		return client;
	}


	@Override
	public void setLogger(CloudLogger logger) {this.logger = logger;	}

	@Override
	public String getCloudSRN() { return strAWSCacheARN; }

}
