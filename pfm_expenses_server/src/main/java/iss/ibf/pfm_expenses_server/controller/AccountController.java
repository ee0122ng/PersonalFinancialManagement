package iss.ibf.pfm_expenses_server.controller;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import iss.ibf.pfm_expenses_server.exception.NoEmailFoundException;
import iss.ibf.pfm_expenses_server.exception.NoUserDetailsFoundException;
import iss.ibf.pfm_expenses_server.exception.UsernameException;
import iss.ibf.pfm_expenses_server.model.User;
import iss.ibf.pfm_expenses_server.service.AccountService;
import iss.ibf.pfm_expenses_server.service.CurrencyConverterService;
import iss.ibf.pfm_expenses_server.service.TransactionService;
import iss.ibf.pfm_expenses_server.service.UploadProfilePictureService;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping(path={"/api"})
public class AccountController {

    @Autowired
    private AccountService accSvc;

    @Autowired
    private UploadProfilePictureService uploadPicSvc;

    @Autowired
    private TransactionService transSvc;

    @Autowired
    private CurrencyConverterService currCnvSvc;
    
    // controller to register user
    @PostMapping(path={"/account/register"}, consumes = MediaType.APPLICATION_JSON_VALUE)
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
    @PostMapping(path={"/account/login"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> authenticateUser(@RequestBody String userLoginDetails, HttpSession session) {
        JsonObject json = this.accSvc.convertStringToJsonObject(userLoginDetails);

        try {
            String username = json.getString("username");
            session.setAttribute("username", username); // save current username to session

            // check if username and account are still valid
            if (this.accSvc.checkUsername(username)) {

                // verify password
                String pwd = json.getString("password");
                Boolean validPwd = this.accSvc.checkPassword(username, pwd);

                if (validPwd) {

                    String accountId = this.accSvc.getUserAccount(username).getAccountId();
                    String userId = this.accSvc.getUserAccount(username).getUserId();
                    session.setAttribute("sessionAccountId", accountId);
                    session.setAttribute("sessionUserId", userId);

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

    // controller to logout all session
    @GetMapping(path={"/account/logout"})
    @ResponseBody
    public ResponseEntity<String> logoutSession(HttpSession session) {

        session.invalidate();
        JsonObject payload = Json.createObjectBuilder().add("payload", "Logout successfully").build();

        return ResponseEntity.status(HttpStatus.OK).body(payload.toString());
    }

    // controller to complete user account
    @PostMapping(path={"/profile"}, consumes=MediaType.APPLICATION_JSON_VALUE)
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

    // controller to retrieve user profile
    @GetMapping(path={"/profile"})
    @ResponseBody
    public ResponseEntity<Object> retrieveProfile(@RequestParam String username, HttpSession session) {
        
        String accountId = (String) session.getAttribute("sessionAccountId");
        String userId = (String) session.getAttribute("sessionUserId");

        try {
            JsonObject payload = this.accSvc.getUserProfile(username, accountId, userId);
            return ResponseEntity.status(HttpStatus.OK).body(payload.toString());

        } catch (Exception ex) {

            JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.toString());
        }
    }

    // controller to upload user profile picture
    @PostMapping(path={"/profile/image-upload"}, consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<String> uploadProfilePicture(@RequestPart MultipartFile profilePic, @RequestPart String username, @RequestPart String accountId, HttpSession session) throws IOException {

        String userId = (String) session.getAttribute("sessionUserId");

        try {
            String endpoint = this.uploadPicSvc.uploadProfilePicture(profilePic, username, accountId, userId);
            JsonObject payload = Json.createObjectBuilder().add("endpoint", endpoint).build();
            return ResponseEntity.status(HttpStatus.OK).body(payload.toString());

        } catch (Exception ex) {
            JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.toString());

        }

    }

    // controller to get currencies list
    @GetMapping(path={"/transaction/currencies"})
    @ResponseBody
    public ResponseEntity<String> getCurrencies() {

        try {
            List<String> currencies = this.transSvc.getCurrencies();
            JsonArray jArr = Json.createArrayBuilder(currencies).build();
            JsonObject payload = Json.createObjectBuilder().add("currencies", jArr).build();

            return ResponseEntity.status(HttpStatus.OK).body(payload.toString());

        } catch( Exception ex) {

            JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.toString());
        }
        
    }

    // controller to insert new transaction
    @PostMapping(path={"/transaction/insert"})
    @ResponseBody
    public ResponseEntity<String> insertTransaction(@RequestBody String form, HttpSession session) {

        String userId = (String) session.getAttribute("sessionUserId");
        JsonObject req = Json.createReader(new StringReader(form)).readObject();

        try {
            Boolean result = this.transSvc.insertTransaction(req, userId);

            if (result) {
                JsonObject message = Json.createObjectBuilder().add("success", "Record insertion succeeded").build();
                return ResponseEntity.status(HttpStatus.OK).body(message.toString());
            } else {
                JsonObject message = Json.createObjectBuilder().add("error", "Record insertion failed").build();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message.toString());
            }
        } catch (Exception ex) {
            System.out.println(">>> exception: " + ex.getMessage());
            JsonObject error  = Json.createObjectBuilder().add("error", ex.getMessage()).build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.toString());
        }

    }

    // ############## Testing Purpose ######################
    // controller to convert the currency 
    @GetMapping(path={"/transaction/converter"})
    @ResponseBody
    public ResponseEntity<String> convertToSGD(@RequestBody String curr) {

        JsonObject req = Json.createReader(new StringReader(curr)).readObject();
        String fromCurrency = req.getString("from");

        try {   
            Double rate = this.currCnvSvc.convertToSGD(fromCurrency);
            JsonObject payload = Json.createObjectBuilder().add("%s_SGD".formatted(fromCurrency), rate).build();

            return ResponseEntity.status(HttpStatus.OK).body(payload.toString());

        } catch (Exception ex) {
            JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error.toString());
        }

    }

}
