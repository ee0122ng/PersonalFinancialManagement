package iss.ibf.pfm_expenses_server.model;

import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public class User {

    private String userId;

    @NotEmpty(message="User name cannot be blank")
    @Pattern(regexp="[a-zA-Z0-9_]{3-10}")
    private String username;

    public User() {
    }

    public User(String username) {
        this.userId = generateUserId();
        this.username = username;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private String generateUserId() {

        return UUID.randomUUID().toString().substring(0, 8);
    }
    
}
