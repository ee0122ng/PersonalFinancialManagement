package iss.ibf.pfm_expenses_server.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import iss.ibf.pfm_expenses_server.exception.NoEmailFoundException;
import iss.ibf.pfm_expenses_server.exception.NoUserDetailsFoundException;
import iss.ibf.pfm_expenses_server.exception.UsernameException;
import iss.ibf.pfm_expenses_server.model.User;
import iss.ibf.pfm_expenses_server.service.AccountService;
import jakarta.json.Json;
import jakarta.json.JsonObject;

@Controller
@RequestMapping(path={"/api"})
public class AccountController {

    @Autowired
    private AccountService accSvc;
    
    // controller to register user
    @PostMapping(path={"/register"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> registerUserAccount(@RequestBody String form) {

        JsonObject json = accSvc.convertStringToJsonObject(form);

        try {
            User user = new User(json.getString("username"));
            String pwd = json.getString("password");

            // TODO: add email sending for account activation
            String email = json.getString("email");

            Optional<String> opt = accSvc.registerUserAccount(user, pwd, email);

            try {
                
                String accId = opt.get();
                JsonObject payload = Json.createObjectBuilder()
                                            .add("accountId", accId)
                                            .build();
                return ResponseEntity.status(HttpStatus.OK).body(payload.toString());

            } catch (UsernameException ex) {

                JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error.toString());

            } catch (Exception ex) {

                JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.toString());

            }

        } catch (Exception ex) {

            JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.toString());
        }
        
    }

    // controller to authenticate user
    @PostMapping(path={"/login"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> authenticateUser(@RequestBody String userLoginDetails) {
        JsonObject json = this.accSvc.convertStringToJsonObject(userLoginDetails);

        try {
            String username = json.getString("username");

            // check if username and account are still valid
            if (this.accSvc.checkUsername(username)) {

                // verify password
                String pwd = json.getString("password");
                Boolean validPwd = this.accSvc.checkPassword(username, pwd);

                if (validPwd) {

                    String accountId = this.accSvc.getUserAccount(username).getAccountId();

                    // check if user info completed
                    // 4 possible outcome: completed + hasEmail, completed + noEmail, incomplete + hasEmail, incomplete + noEmail
                    try {
                        Boolean accCompleted = this.accSvc.checkAccountCompletion(username);
                        String email = this.accSvc.getUserEmail(username);
                        JsonObject payload = Json.createObjectBuilder()
                                                    .add("accCompleted", accCompleted)
                                                    .add("accountId", accountId)
                                                    .add("email", email)
                                                    .build();
                        return ResponseEntity.status(HttpStatus.OK).body(payload.toString());

                    } catch (NoUserDetailsFoundException ex) {
                        JsonObject payload = Json.createObjectBuilder()
                                                    .add("accCompleted", false)
                                                    .add("accountId", accountId)
                                                    .add("email", "")
                                                    .build();
                        return ResponseEntity.status(HttpStatus.OK).body(payload.toString());
                    
                    } catch (NoEmailFoundException ex) {
                        Boolean accCompleted = this.accSvc.checkAccountCompletion(username);
                        JsonObject payload = Json.createObjectBuilder()
                                                    .add("accCompleted", accCompleted)
                                                    .add("accountId", accountId)
                                                    .add("email", "")
                                                    .build();
                        return ResponseEntity.status(HttpStatus.OK).body(payload.toString());

                    } catch (Exception ex) {

                        JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();

                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error.toString());

                    }

                } else {

                    JsonObject error = Json.createObjectBuilder().add("error", "Login failed").build();

                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error.toString());

                }

            } else {

                JsonObject error = Json.createObjectBuilder().add("error", "Username is invalid").build();

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error.toString());
            }

        } catch (Exception ex) {

            JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error.toString());

        }

    }

    // controller to complete user account
    @PostMapping(path={"/complete"}, consumes=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> completeUserInfo(@RequestBody String userInfoForm) {

        JsonObject jsonUserInfo = this.accSvc.convertStringToJsonObject(userInfoForm);

        try {
            Boolean userInfoUpdated = this.accSvc.completeUserAccount(jsonUserInfo);

            if (userInfoUpdated) {
                JsonObject payload = Json.createObjectBuilder().add("payload", "Update succeeded").build();
                return ResponseEntity.status(HttpStatus.OK).body(payload.toString());
            } else {
                JsonObject error = Json.createObjectBuilder().add("error", "Update failed").build();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.toString());
            }

        } catch (Exception ex) {
            
            JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error.toString());
        }
    }
}
