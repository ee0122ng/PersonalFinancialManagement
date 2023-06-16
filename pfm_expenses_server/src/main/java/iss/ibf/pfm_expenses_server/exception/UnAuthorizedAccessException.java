package iss.ibf.pfm_expenses_server.exception;

public class UnAuthorizedAccessException extends RuntimeException {

    public UnAuthorizedAccessException() {}

    public UnAuthorizedAccessException(String message) {
        super(message);
    }
    
}
