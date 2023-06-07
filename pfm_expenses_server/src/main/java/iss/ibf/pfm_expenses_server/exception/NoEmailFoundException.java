package iss.ibf.pfm_expenses_server.exception;

public class NoEmailFoundException extends RuntimeException {

    public NoEmailFoundException() {}

    public NoEmailFoundException(String message) {
        super(message);
    }
    
}
