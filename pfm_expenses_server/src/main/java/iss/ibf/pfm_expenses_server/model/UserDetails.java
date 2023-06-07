package iss.ibf.pfm_expenses_server.model;

import java.util.Date;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public class UserDetails {

    private String userId;

    private Integer id;

    @NotEmpty(message="Firstname cannot be blank")
    private String firstname;
    
    private String lastname;

    @Email(message="Invalid email format")
    private String email;

    @DateTimeFormat(pattern="yyyy-MM-dd")
    private Date dob;

    private Integer age;
    private String occupation;
    private String country;

    public UserDetails() {}

    public UserDetails(String userId) {
        this.userId = userId;
    }

    public UserDetails(String userId, String email) {
        this(userId);
        this.email = email;
    }

    public UserDetails(String firstname, String lastname, Date dob, String occupation, String country) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.setDob(dob);
        this.occupation = occupation;
        this.country = country;
    }

    public UserDetails(String userId, String firstname, String lastname, String email, Date dob, Integer age, String occupation, String country) {
        this(userId, email);
        this.firstname = firstname;
        this.lastname = lastname;
        this.dob = dob;
        this.occupation = occupation;
        this.country = country;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
        this.age = Period.between(dob.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), LocalDate.now()).getYears();
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

    @Override
    public String toString() {
        return "{" +
            " userId='" + getUserId() + "'" +
            ", id='" + getId() + "'" +
            ", firstname='" + getFirstname() + "'" +
            ", lastname='" + getLastname() + "'" +
            ", email='" + getEmail() + "'" +
            ", dob='" + getDob() + "'" +
            ", age='" + getAge() + "'" +
            ", occupation='" + getOccupation() + "'" +
            ", country='" + getCountry() + "'" +
            "}";
    }

    
}
