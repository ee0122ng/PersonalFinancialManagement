package iss.ibf.pfm_expenses_server.exception;

public class S3UploadFailedException extends RuntimeException {

    public S3UploadFailedException() {}

    public S3UploadFailedException(String message) {
        super(message);
    }
    
}
