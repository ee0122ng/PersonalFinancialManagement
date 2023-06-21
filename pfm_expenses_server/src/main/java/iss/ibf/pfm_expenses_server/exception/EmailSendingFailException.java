package iss.ibf.pfm_expenses_server.exception;

public class EmailSendingFailException extends RuntimeException {

    public EmailSendingFailException() {}

    public EmailSendingFailException(String message) {
        super(message);
    }
    
}
