package iss.ibf.pfm_expenses_server.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import iss.ibf.pfm_expenses_server.repository.S3BucketRepository;

@Service
public class UploadProfilePictureService {

    @Autowired
    private S3BucketRepository s3BucketRepo;

    public String uploadProfilePicture(MultipartFile image, String username, String accountId, String userId) throws IOException {

        return s3BucketRepo.uploadPicture(image, username, accountId, userId);
    }
    
}
