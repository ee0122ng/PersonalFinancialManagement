package iss.ibf.pfm_expenses_server.exception;

public class AccountCreationException extends RuntimeException {

    public AccountCreationException() {}

    public AccountCreationException(String message) {

        super(message);
    }
    
}
