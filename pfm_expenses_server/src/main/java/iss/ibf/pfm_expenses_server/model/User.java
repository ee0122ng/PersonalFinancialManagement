package iss.ibf.pfm_expenses_server.model;

import java.util.Date;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public class User {

    private String userId;

    @NotEmpty(message="User name cannot be blank")
    @Pattern(regexp="[a-zA-Z0-9_]{3-10}")
    private String username;

    private Integer id;

    @NotEmpty(message="Firstname cannot be blank")
    private String firstname;

    @NotEmpty(message="Lastname cannot be blank")
    private String lastname;

    @Email(message="Invalid email format")
    private String email;

    @DateTimeFormat(pattern="MM-dd-yyyy")
    private Date dob;

    private Integer age;
    private String occupation;
    private String country;

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

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstname() {
        return this.firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return this.lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getDob() {
        return this.dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public Integer getAge() {
        return this.age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getOccupation() {
        return this.occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    private String generateUserId() {

        return UUID.randomUUID().toString().substring(0, 8);
    }
    
}
