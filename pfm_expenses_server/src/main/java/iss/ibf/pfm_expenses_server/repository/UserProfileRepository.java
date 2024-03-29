package iss.ibf.pfm_expenses_server.repository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;

import iss.ibf.pfm_expenses_server.exception.NoEmailFoundException;
import iss.ibf.pfm_expenses_server.exception.NoUserDetailsFoundException;
import iss.ibf.pfm_expenses_server.exception.UserDetailsException;
import iss.ibf.pfm_expenses_server.exception.UsernameException;
import iss.ibf.pfm_expenses_server.model.Account;
import iss.ibf.pfm_expenses_server.model.User;
import iss.ibf.pfm_expenses_server.model.UserDetails;
import jakarta.json.Json;
import jakarta.json.JsonObject;

@Repository
public class UserProfileRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${do.storage.endpoint}")
    private String ENDPOINT;

    @Value("${do.storage.endpoint.region}")
    private String REGION;

    @Value("${do.storage.bucketname}")
    private String BUCKET;

    @Autowired
    private AmazonS3 s3;

    private final String GET_USER_INFO_SQL = "select * from user_details where user_id=?";
    private final String GET_USER_ID_BY_USERNAME_SQL = "select * from users where username=?";
    private final String GET_ACCOUNT_BY_ID_SQL = "select * from accounts where account_id=?";
    private final String UPDATE_USER_INFO_SQL = "update user_details set firstname=?, lastname=?, email=?, dob=?, age=?, country=?, occupation=? where user_id=?";
    private final String INSETR_USER_INFO_SQL = "insert into user_details(user_id, firstname, lastname, email, dob, age, country, occupation) values(?, ?, ?, ?, ?, ?, ?, ?)";
    private final String GET_USER_ID_BY_ACCID_SQL = "select * from accounts where account_id=?";
    private final String GET_EMAIL_BY_USERNAME = "select email from user_details where username=?";

    public Boolean verifyAccountCompletionStatus(String username) {

        Optional<String> opt = this.getUserIdByUsername(username);

        if (opt.isEmpty()) {
            throw new UsernameException("No matching username found");

        } else {

            String userId = opt.get();

            //return true if there is user details id, email and firstname found
            try {
                UserDetails userDetails = this.jdbcTemplate.queryForObject(GET_USER_INFO_SQL, BeanPropertyRowMapper.newInstance(UserDetails.class), userId);
                return userDetails.getId() != null && !userDetails.getEmail().isBlank() && !userDetails.getFirstname().isBlank();

            } catch (Exception ex) {
                // return false if no user details found at all
                throw new NoUserDetailsFoundException("No user details found");
            }

        }

    }

    // get userid after account successfully created
    public Optional<String> getUserIdByUsername(String username) {

        List<User> user = jdbcTemplate.query(GET_USER_ID_BY_USERNAME_SQL, BeanPropertyRowMapper.newInstance(User.class), username);
        
        return user.size() > 0 ? Optional.of(user.get(0).getUserId()) : Optional.empty();
    }

    // update user details if matching user id found
    // insert user details if no matching user id
    public Boolean updateUserDetails(JsonObject form) throws ParseException {

        Account acc = jdbcTemplate.queryForObject(GET_ACCOUNT_BY_ID_SQL, BeanPropertyRowMapper.newInstance(Account.class), form.getString("accountId"));
        String userId = acc.getUserId();

        try {
            // code below throw errors
            UserDetails userDetails = populateFormToUserDetails(form);

            return jdbcTemplate.update(UPDATE_USER_INFO_SQL, userDetails.getFirstname(), userDetails.getLastname(), userDetails.getEmail() ,userDetails.getDob(), userDetails.getAge(), userDetails.getCountry(), userDetails.getOccupation(), userId) > 0;
            
        } catch (Exception ex) {
            
            throw ex;

        } 

    }

    // public UserDetails getUserDetailsByUserId(String userId) {

    //     try {

    //         UserDetails userDetails = jdbcTemplate.queryForObject(GET_USER_INFO_SQL, BeanPropertyRowMapper.newInstance(UserDetails.class), userId);
    //         return userDetails;

    //     } catch (Exception ex) {

    //         throw new UserDetailsException("No matching user details for user_id=%s".formatted(userId));
    //     }
        
    // }

    // insert user details

    public UserDetails populateFormToUserDetails(JsonObject form) throws ParseException {

        UserDetails userDetails = new UserDetails();

        userDetails.setEmail(form.getString("email", ""));
        userDetails.setFirstname(form.getString("firstname"));
        userDetails.setLastname(form.getString("lastname", ""));
        userDetails.setCountry(form.getString("country", ""));
        userDetails.setOccupation(form.getString("occupation", ""));

        if (!form.getString("dob").isEmpty()) {
            Date dob = new SimpleDateFormat("yyyy-MM-dd").parse(form.getString("dob"));
            userDetails.setDob(new java.sql.Date(dob.getTime()));
        }
    
        return userDetails;

    }

    public String getUserEmailByUsername(String username) {

        Optional<String> opt = this.getUserIdByUsername(username);

        try {
            String userId = opt.get();

            String email = "";
            final SqlRowSet rs = jdbcTemplate.queryForRowSet(GET_USER_INFO_SQL, userId);
            while (rs.next()) {
                email = rs.getString("email");
            }

            // UserDetails userDetails = this.getUserDetailsByUserId(userId);
            return Optional.of(email).orElseThrow(() -> new NoEmailFoundException());

        } catch (Exception ex) {
            throw new NoEmailFoundException("No user email found");

        }
        
    }

    public JsonObject retrieveUserProfile(String username, String accountId, String userId) {

        UserDetails userDetails = this.jdbcTemplate.queryForObject(GET_USER_INFO_SQL, BeanPropertyRowMapper.newInstance(UserDetails.class), this.getUserIdByUsername(username).get());
        String userProfileUrl = "";
        String fileDirectory = "";

        // retrieve user profile pic from S3
        if(null != userDetails.getImageUrl()) {
            userProfileUrl = userDetails.getImageUrl();
        }

        try {
            // get the file directory from the endpoint string
            Pattern pattern  = Pattern.compile("(?<=https://).*");
            Matcher matcher = pattern.matcher(userProfileUrl);
            
            // note: match.find() will only return true once
            if (matcher.find()) {
                String matching = matcher.group().toString();
                String[] arr = matching.split("/");
                fileDirectory = arr[1] + "/" + arr[2];
            }

            GetObjectRequest getReq = new GetObjectRequest(BUCKET, fileDirectory);
    
        } catch (AmazonS3Exception ex) {
            throw ex;
        } catch (Exception ex) {
            throw ex;
        }

         return this.convertProfileToJson(userDetails, userProfileUrl);
        
    }
    
    public JsonObject convertProfileToJson(UserDetails profile, String profileUrl) {

        JsonObject json = Json.createObjectBuilder()
                                .add("firstname", profile.getFirstname())
                                .add("lastname", profile.getLastname())
                                .add("email", profile.getEmail())
                                .add("country", profile.getCountry())
                                .add("dob", profile.getDob().toString())
                                .add("age", profile.getAge())
                                .add("occupation", profile.getOccupation())
                                .add("profileUrl", profileUrl)
                                .build();

        return json;
    }
    
}
