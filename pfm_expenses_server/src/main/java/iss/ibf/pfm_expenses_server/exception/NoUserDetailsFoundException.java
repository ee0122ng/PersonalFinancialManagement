package iss.ibf.pfm_expenses_server.exception;

public class NoUserDetailsFoundException extends RuntimeException {

    public NoUserDetailsFoundException() {}

    public NoUserDetailsFoundException(String message) {
        super(message);
    }
    
}
