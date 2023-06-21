package iss.ibf.pfm_expenses_server.model;

import java.io.Serializable;

public class UserSecureEntity implements Serializable {
    
    private String username;
    private String hashPwd;

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHashPwd() {
        return this.hashPwd;
    }

    public void setHashPwd(String hashPwd) {
        this.hashPwd = hashPwd;
    }

    
}
