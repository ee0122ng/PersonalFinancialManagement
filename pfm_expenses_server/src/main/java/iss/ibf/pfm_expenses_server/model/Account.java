package iss.ibf.pfm_expenses_server.model;

import java.util.UUID;

public class Account {

    private String accountId;
    private String userId;
    private String saltString;
    private String hashedString;
    private Boolean isValid;
    private String deletionToken;

    public Account(String userId) {
        this.accountId = generateAccountId();
        this.saltString = generateSaltString();
        this.isValid = true;
    }

    public String getAccountId() {
        return this.accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSaltString() {
        return this.saltString;
    }

    public void setSaltString(String saltString) {
        this.saltString = saltString;
    }

    public String getHashedString() {
        return this.hashedString;
    }

    public void setHashedString(String hashedString) {
        this.hashedString = hashedString;
    }

    public Boolean isIsValid() {
        return this.isValid;
    }

    public Boolean getIsValid() {
        return this.isValid;
    }

    public void setIsValid(Boolean isValid) {
        this.isValid = isValid;
    }

    public String getDeletionToken() {
        return this.deletionToken;
    }

    public void setDeletionToken(String deletionToken) {
        this.deletionToken = deletionToken;
    }

    private String generateAccountId() {

        return UUID.randomUUID().toString().substring(0,8);
    }

    private String generateSaltString() {

        return UUID.randomUUID().toString().substring(0, 24);
    }
    
}
