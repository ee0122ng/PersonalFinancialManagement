package iss.ibf.pfm_expenses_server.config;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.BucketVersioningConfiguration;
import com.amazonaws.services.s3.model.SetBucketVersioningConfigurationRequest;

@Configuration
public class S3Config {

    @Value("${do.storage.key}")
    private String accessKey;
    @Value("${do.storage.secretkey}")
    private String secretKey;
    @Value("${do.storage.endpoint}")
    private String endpoint;
    @Value("${do.storage.endpoint.region}")
    private String endpointRegion;
    @Value("${do.storage.bucketname}")
    private String bucketname;

    private Logger logger = Logger.getLogger("S3Logger");
    
    @Bean
    public AmazonS3 createS3Client() {
        BasicAWSCredentials cred = new BasicAWSCredentials(accessKey, secretKey);
        EndpointConfiguration epConfig = new EndpointConfiguration(endpoint, endpointRegion);
        AmazonS3 s3Client = AmazonS3ClientBuilder
            .standard()
            .withEndpointConfiguration(epConfig)
            .withCredentials(new AWSStaticCredentialsProvider(cred))
            .build();

        try {

            // 1. Enable versioning on the bucket.
        	BucketVersioningConfiguration configuration = 
        		new BucketVersioningConfiguration().withStatus("Enabled");
            
			SetBucketVersioningConfigurationRequest setBucketVersioningConfigurationRequest = 
				new SetBucketVersioningConfigurationRequest(bucketname, configuration);
			
			s3Client.setBucketVersioningConfiguration(setBucketVersioningConfigurationRequest);
			
			// 2. Get bucket versioning configuration information.
			BucketVersioningConfiguration conf = s3Client.getBucketVersioningConfiguration(bucketname);
            logger.log(Level.INFO, ">>> bucket versioning configuration status: %s".formatted(conf.getStatus()));

        } catch (AmazonS3Exception amazonS3Exception) {
            logger.log(Level.INFO, "An Amazon S3 error occurred. Exception: %s".formatted(amazonS3Exception.toString()));
        } catch (Exception ex) {
            logger.log(Level.INFO, "Exception: %s".formatted(ex.toString()));
        } 
        
        return s3Client;
    }
    
}
