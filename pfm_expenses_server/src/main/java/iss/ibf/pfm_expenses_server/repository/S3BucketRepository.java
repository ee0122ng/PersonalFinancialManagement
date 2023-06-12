package iss.ibf.pfm_expenses_server.repository;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import iss.ibf.pfm_expenses_server.exception.S3UploadFailedException;

@Repository
public class S3BucketRepository {

    @Value("${do.storage.endpoint}")
    private String ENDPOINT;

    @Value("${do.storage.endpoint.region}")
    private String REGION;

    @Value("${do.storage.bucketname}")
    private String BUCKET;

    @Autowired
    private AmazonS3 s3;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final String UPDATE_PROFILE_PIC_URL_SQL = "update user_details set image_url=? where user_id=?";

    public String uploadPicture(MultipartFile image, String username, String accountId, String userId) throws IOException {

        Map<String, String> userData = new HashMap<>();
        userData.put("uploadedDate", new SimpleDateFormat("dd MMM, yyyy HH:mm:ss").format(new Date()));
        userData.put("accountID", accountId);
        userData.put("username", username);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(image.getContentType());
        metadata.setContentLength(image.getSize());
        metadata.setUserMetadata(userData);

        String fileDirectory = "%s/%s".formatted(accountId, "profile_picture_" + username + "." + image.getContentType().split("/")[1]);

        PutObjectRequest putReq = new PutObjectRequest(BUCKET, fileDirectory, image.getInputStream(), metadata);
        putReq = putReq.withCannedAcl(CannedAccessControlList.PublicRead);

        try {
            s3.putObject(putReq);
            String endpoint = "https://" + BUCKET + "." + ENDPOINT + "/" + fileDirectory;
            
            // update user_detail table for the profile picture
            this.jdbcTemplate.update(UPDATE_PROFILE_PIC_URL_SQL, endpoint, userId);

            return endpoint;

        } catch (Exception ex) {
            System.out.println(">>> exception: " + ex.getMessage());
            throw new S3UploadFailedException("Fail to upload profile picture");
        }

    }

}
