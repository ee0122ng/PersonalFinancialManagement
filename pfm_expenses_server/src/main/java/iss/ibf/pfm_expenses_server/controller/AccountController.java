package iss.ibf.pfm_expenses_server.controller;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.el.parser.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import iss.ibf.pfm_expenses_server.authentication.JwtTokenUtil;
import iss.ibf.pfm_expenses_server.authentication.MyAuthenticationManager;
import iss.ibf.pfm_expenses_server.exception.NoEmailFoundException;
import iss.ibf.pfm_expenses_server.exception.NoUserDetailsFoundException;
import iss.ibf.pfm_expenses_server.exception.UnAuthorizedAccessException;
import iss.ibf.pfm_expenses_server.exception.UsernameException;
import iss.ibf.pfm_expenses_server.model.User;
import iss.ibf.pfm_expenses_server.service.AccountService;
import iss.ibf.pfm_expenses_server.service.CurrencyConverterService;
import iss.ibf.pfm_expenses_server.service.GoogleApiService;
import iss.ibf.pfm_expenses_server.service.TransactionService;
import iss.ibf.pfm_expenses_server.service.UploadProfilePictureService;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping()
@CrossOrigin(origins = "*")
public class AccountController {

    @Autowired
    private AccountService accSvc;

    @Autowired
    private UploadProfilePictureService uploadPicSvc;

    @Autowired
    private TransactionService transSvc;

    @Autowired
    private CurrencyConverterService currCnvSvc;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private MyAuthenticationManager authManager;

    @Autowired
    private GoogleApiService gapiSvc;

    private Logger logger = Logger.getLogger(AccountController.class.getName());

    private HashMap<String, HashMap<String, String>> sessionMap = new LinkedHashMap<String, HashMap<String, String>>();
    // private HashMap<String, String> sessionJwt = new LinkedHashMap<String, String>(); 
    
    // controller to register user
    @PostMapping(path={"/api/account/register"}, consumes = MediaType.APPLICATION_JSON_VALUE)
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
                
                this.logger.log(Level.WARNING, ex.getLocalizedMessage());
                JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error.toString());

            } catch (Exception ex) {

                this.logger.log(Level.WARNING, ex.getLocalizedMessage());
                JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.toString());

            }

        } catch (Exception ex) {

            this.logger.log(Level.WARNING, ex.getLocalizedMessage());
            JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.toString());
        }
        
    }

    // controller to authenticate user
    @PostMapping(path={"/api/account/login"}, consumes = MediaType.APPLICATION_JSON_VALUE)
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
                    String userId = this.accSvc.getUserAccount(username).getUserId();

                    // authenticate the user login by creating a security context
                    Authentication authentication = this.authManager.generateAuthEntity(username, pwd);
                    String jwt = this.jwtTokenUtil.generateJwtToken(username);

                    // store user info as a map in the session
                    HashMap<String, String> userInfo = new HashMap<>();
                    userInfo.put("accountId", accountId);
                    userInfo.put("userId", userId);
                    userInfo.put("jwt", jwt);
                    this.sessionMap.put(username, userInfo);

                    // check if user info completed
                    // 4 possible outcome: completed + hasEmail, completed + noEmail, incomplete + hasEmail, incomplete + noEmail
                    try {
                        Boolean accCompleted = this.accSvc.checkAccountCompletion(username);
                        String email = this.accSvc.getUserEmail(username);

                        JsonObject payload = Json.createObjectBuilder()
                                                    .add("accCompleted", accCompleted)
                                                    .add("accountId", accountId)
                                                    .add("email", email)
                                                    .add("token", jwt)
                                                    .build();
                                                    
                        return ResponseEntity.status(HttpStatus.OK).body(payload.toString());

                    } catch (NoUserDetailsFoundException ex) {
                        this.logger.log(Level.INFO, ex.getLocalizedMessage());

                        String email = this.accSvc.getUserEmail(username);
                        JsonObject payload = Json.createObjectBuilder()
                                                    .add("accCompleted", false)
                                                    .add("accountId", accountId)
                                                    .add("email", email)
                                                    .add("token", jwt)
                                                    .build();
                        return ResponseEntity.status(HttpStatus.OK).body(payload.toString());
                    
                    } catch (NoEmailFoundException ex) {
                        this.logger.log(Level.INFO, ex.getLocalizedMessage());
                        Boolean accCompleted = this.accSvc.checkAccountCompletion(username);
                        JsonObject payload = Json.createObjectBuilder()
                                                    .add("accCompleted", accCompleted)
                                                    .add("accountId", accountId)
                                                    .add("email", "")
                                                    .add("token", jwt)
                                                    .build();
                        return ResponseEntity.status(HttpStatus.OK).body(payload.toString());

                    } catch (Exception ex) {
                        this.logger.log(Level.WARNING, ex.getLocalizedMessage());
                        JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();

                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error.toString());

                    }

                } else {

                    this.logger.log(Level.INFO, "Password invalid");
                    JsonObject error = Json.createObjectBuilder().add("error", "Login failed").build();

                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error.toString());

                }

            } else {
                this.logger.log(Level.INFO, "Username invalid");
                JsonObject error = Json.createObjectBuilder().add("error", "Username is invalid").build();

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error.toString());
            }

        } catch (Exception ex) {

            this.logger.log(Level.WARNING, ex.getLocalizedMessage());
            JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error.toString());

        }

    }

    // controller to logout all session
    @PostMapping(path={"/api/account/logout"})
    @ResponseBody
    public ResponseEntity<String> logoutSession(@RequestHeader("Jwtauth") String authorizationHeader) {

        // check if token valid
        if (!this.jwtTokenUtil.validateJwtToken(authorizationHeader.substring(7))) {
            this.logger.log(Level.INFO, "Invalid jwt token");
            JsonObject error = Json.createObjectBuilder().add("error", "Invalid token").build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }
        // check if username in session
        String username = this.jwtTokenUtil.getUserNameFromJwtToken(authorizationHeader.substring(7));
        if (!this.sessionMap.containsKey(username)) {
            this.logger.log(Level.INFO, "Invalid user session");
            JsonObject error = Json.createObjectBuilder().add("error", "Access denied").build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }

        this.sessionMap.remove(username);
        JsonObject payload = Json.createObjectBuilder().add("payload", "Logout successfully").build();

        return ResponseEntity.status(HttpStatus.OK).body(payload.toString());
    }

    // controller to retrieve username if token still valid
    @GetMapping(path={"/api/account/validsession"})
    @ResponseBody
    public ResponseEntity<String> getUserLastSession(@RequestHeader("Jwtauth") String authorizationHeader) {
        // check if token valid
        if (!this.jwtTokenUtil.validateJwtToken(authorizationHeader.substring(7))) {
            this.logger.log(Level.INFO, "Invalid jwt token");
            JsonObject error = Json.createObjectBuilder().add("error", "Invalid token").build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }
        String username = this.jwtTokenUtil.getUserNameFromJwtToken(authorizationHeader.substring(7));

        JsonObject payload = Json.createObjectBuilder().add("username", username).build();
        return ResponseEntity.status(HttpStatus.OK).body(payload.toString());

    }

    // controller to complete user account - new user
    @PostMapping(path={"/api/profile"}, consumes=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> insertUserInfo(@RequestBody String userInfoForm) {

        JsonObject jsonUserInfo = this.accSvc.convertStringToJsonObject(userInfoForm);

        try {
            Boolean userInfoUpdated = this.accSvc.completeUserAccount(jsonUserInfo);

            if (userInfoUpdated) {
                JsonObject payload = Json.createObjectBuilder().add("payload", "Update succeeded").build();
                return ResponseEntity.status(HttpStatus.OK).body(payload.toString());
            } else {
                this.logger.log(Level.WARNING, "Update user account failed");
                JsonObject error = Json.createObjectBuilder().add("error", "Update failed").build();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.toString());
            }

        } catch (UnAuthorizedAccessException ex) {
            this.logger.log(Level.WARNING, ex.getLocalizedMessage());
            JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());

        } catch (Exception ex) {
            this.logger.log(Level.WARNING, ex.getLocalizedMessage());
            JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error.toString());
        } 
    }

    // controller to complete user account - registered user
    @PutMapping(path={"/api/profile"}, consumes=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> completeUserInfo(@RequestHeader("Jwtauth") String authorizationHeader, @RequestBody String userInfoForm) {

        // check if token valid
        if (!this.jwtTokenUtil.validateJwtToken(authorizationHeader.substring(7))) {
            this.logger.log(Level.INFO, "Invalid jwt token");
            JsonObject error = Json.createObjectBuilder().add("error", "Invalid token").build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }
        // check if username in session
        String username = this.jwtTokenUtil.getUserNameFromJwtToken(authorizationHeader.substring(7));
        if (!this.sessionMap.containsKey(username)) {
            this.logger.log(Level.INFO, "Invalid user session");
            JsonObject error = Json.createObjectBuilder().add("error", "Access denied").build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }

        JsonObject jsonUserInfo = this.accSvc.convertStringToJsonObject(userInfoForm);
        System.out.println(">>> sample request: " + jsonUserInfo.toString());

        try {
            Boolean userInfoUpdated = this.accSvc.completeUserAccount(jsonUserInfo);


            if (userInfoUpdated) {
                JsonObject payload = Json.createObjectBuilder().add("payload", "Update succeeded").build();
                return ResponseEntity.status(HttpStatus.OK).body(payload.toString());
            } else {
                this.logger.log(Level.WARNING, "Update user account failed");
                JsonObject error = Json.createObjectBuilder().add("error", "Update failed").build();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.toString());
            }

        } catch (UnAuthorizedAccessException ex) {
            this.logger.log(Level.WARNING, ex.getLocalizedMessage());
            JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());

        } catch (Exception ex) {
            this.logger.log(Level.WARNING, ex.getLocalizedMessage());
            JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error.toString());
        } 
    }

    // controller to retrieve user profile
    @GetMapping(path={"/api/profile"}, produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Object> retrieveProfile(HttpServletRequest request, @RequestHeader("Jwtauth") String authorizationHeader) {

        // check if token valid
        if (!this.jwtTokenUtil.validateJwtToken(authorizationHeader.substring(7))) {
            this.logger.log(Level.INFO, "Invalid jwt token");
            JsonObject error = Json.createObjectBuilder().add("error", "Invalid token").build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }
        // check if username in session
        String username = this.jwtTokenUtil.getUserNameFromJwtToken(authorizationHeader.substring(7));
        if (!this.sessionMap.containsKey(username)) {
            this.logger.log(Level.INFO, "Invalid user session");
            JsonObject error = Json.createObjectBuilder().add("error", "Access denied").build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }
        String accountId = this.sessionMap.get(username).get("accountId");
        String userId = this.sessionMap.get(username).get("userId");

        try {
            JsonObject payload = this.accSvc.getUserProfile(username, accountId, userId);
            return ResponseEntity.status(HttpStatus.OK).body(payload.toString());

        } catch (UnAuthorizedAccessException ex) {
            this.logger.log(Level.WARNING, ex.getLocalizedMessage());
            JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        } catch (Exception ex) {
            this.logger.log(Level.WARNING, ex.getLocalizedMessage());
            JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.toString());
        }
    }

    // controller to upload user profile picture
    @PostMapping(path={"/api/profile/image-upload"}, consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<String> uploadProfilePicture(@RequestHeader("Jwtauth") String authorizationHeader, @RequestPart MultipartFile profilePic) throws IOException {

        // check if token valid
        if (!this.jwtTokenUtil.validateJwtToken(authorizationHeader.substring(7))) {
            this.logger.log(Level.INFO, "Invalid jwt token");
            JsonObject error = Json.createObjectBuilder().add("error", "Invalid token").build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }
        // check if username in session
        String username = this.jwtTokenUtil.getUserNameFromJwtToken(authorizationHeader.substring(7));
        if (!this.sessionMap.containsKey(username)) {
            this.logger.log(Level.INFO, "Invalid user session");
            JsonObject error = Json.createObjectBuilder().add("error", "Access denied").build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }
        String accountId = this.sessionMap.get(username).get("accountId");
        String userId = this.sessionMap.get(username).get("userId");

        try {
            String endpoint = this.uploadPicSvc.uploadProfilePicture(profilePic, username, accountId, userId);
            JsonObject payload = Json.createObjectBuilder().add("endpoint", endpoint).build();
            return ResponseEntity.status(HttpStatus.OK).body(payload.toString());

        } catch (UnAuthorizedAccessException ex) {
            this.logger.log(Level.WARNING, ex.getLocalizedMessage());
            JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        } 
        catch (Exception ex) {
            this.logger.log(Level.WARNING, ex.getLocalizedMessage());
            JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.toString());

        }

    }

    // controller to get currencies list
    @GetMapping(path={"/api/transaction/currencies"})
    @ResponseBody
    public ResponseEntity<String> getCurrencies() {

        try {
            List<String> currencies = this.transSvc.getCurrencies();
            JsonArray jArr = Json.createArrayBuilder(currencies).build();
            JsonObject payload = Json.createObjectBuilder().add("currencies", jArr).build();

            return ResponseEntity.status(HttpStatus.OK).body(payload.toString());

        } catch( Exception ex) {
            this.logger.log(Level.WARNING, ex.getLocalizedMessage());
            JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.toString());
        }
        
    }

    // controller to insert new transaction
    @PostMapping(path={"/api/transaction/insert"})
    @ResponseBody
    public ResponseEntity<String> insertTransaction(@RequestHeader("Jwtauth") String authorizationHeader, @RequestBody String form) {

        // check if token valid
        if (!this.jwtTokenUtil.validateJwtToken(authorizationHeader.substring(7))) {
            this.logger.log(Level.INFO, "Invalid jwt token");
            JsonObject error = Json.createObjectBuilder().add("error", "Invalid token").build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }
        // check if username in session
        String username = this.jwtTokenUtil.getUserNameFromJwtToken(authorizationHeader.substring(7));
        if (!this.sessionMap.containsKey(username)) {
            this.logger.log(Level.INFO, "Invalid user session");
            JsonObject error = Json.createObjectBuilder().add("error", "Access denied").build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }
        String accountId = this.sessionMap.get(username).get("accountId");
        String userId = this.sessionMap.get(username).get("userId");
        JsonObject req = Json.createReader(new StringReader(form)).readObject();

        try {
            Boolean result = this.transSvc.insertTransaction(req, userId);

            if (result) {
                JsonObject message = Json.createObjectBuilder().add("success", "Record insertion succeeded").build();
                return ResponseEntity.status(HttpStatus.OK).body(message.toString());
            } else {
                this.logger.log(Level.WARNING, "Record insertion failed");
                JsonObject message = Json.createObjectBuilder().add("error", "Record insertion failed").build();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message.toString());
            }
        } catch (UnAuthorizedAccessException ex) {
            this.logger.log(Level.WARNING, ex.getLocalizedMessage());
            JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString()); 
        } catch (Exception ex) {
            this.logger.log(Level.WARNING, ex.getLocalizedMessage());
            JsonObject error  = Json.createObjectBuilder().add("error", ex.getMessage()).build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.toString());
        }

    }

    // controller to retrieve transaction record by month
    @GetMapping(path={"/api/transaction/retrieve/{month}"})
    @ResponseBody
    public ResponseEntity<String> retrieveTransactionRecordByMonth(@RequestHeader("Jwtauth") String authorizationHeader, @PathVariable String month) {

        // check if token valid
        if (!this.jwtTokenUtil.validateJwtToken(authorizationHeader.substring(7))) {
            this.logger.log(Level.INFO, "Invalid jwt token");
            JsonObject error = Json.createObjectBuilder().add("error", "Invalid token").build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }
        // check if username in session
        String username = this.jwtTokenUtil.getUserNameFromJwtToken(authorizationHeader.substring(7));
        if (!this.sessionMap.containsKey(username)) {
            this.logger.log(Level.INFO, "Invalid user session");
            JsonObject error = Json.createObjectBuilder().add("error", "Access denied").build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }
        String accountId = this.sessionMap.get(username).get("accountId");
        String userId = this.sessionMap.get(username).get("userId");

        try {
            JsonObject payload = this.transSvc.retrieveTransaction(Integer.valueOf(month), userId);
            return ResponseEntity.status(HttpStatus.OK).body(payload.toString());

        } catch (UnAuthorizedAccessException ex) {
            this.logger.log(Level.WARNING, ex.getLocalizedMessage());
            JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString()); 
        } catch (Exception ex) {
            System.out.println(">>> exception: " + ex.getMessage());
            this.logger.log(Level.WARNING, ex.getLocalizedMessage());
            JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.toString());
        }

    }

    // controller to update transaction record by id
    @PutMapping(path={"/api/transaction/update"})
    @ResponseBody
    public ResponseEntity<String> updateTransactionRecordById(@RequestHeader("Jwtauth") String authorizationHeader, @RequestBody String editForm) {

        // check if token valid
        if (!this.jwtTokenUtil.validateJwtToken(authorizationHeader.substring(7))) {
            this.logger.log(Level.INFO, "Invalid jwt token");
            JsonObject error = Json.createObjectBuilder().add("error", "Invalid token").build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }
        // check if username in session
        String username = this.jwtTokenUtil.getUserNameFromJwtToken(authorizationHeader.substring(7));
        if (!this.sessionMap.containsKey(username)) {
            this.logger.log(Level.INFO, "Invalid user session");
            JsonObject error = Json.createObjectBuilder().add("error", "Access denied").build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }
        String accountId = this.sessionMap.get(username).get("accountId");
        String userId = this.sessionMap.get(username).get("userId");
        JsonObject record = Json.createReader(new StringReader(editForm)).readObject();

        try {
            if (this.transSvc.updateTransaction(record, userId)) {
                JsonObject success = Json.createObjectBuilder().add("success", "Record updated").build();
                return ResponseEntity.status(HttpStatus.OK).body(success.toString());
            } else {
                this.logger.log(Level.WARNING, "Record update failed");
                JsonObject fail = Json.createObjectBuilder().add("fail", "Update failed").build();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(fail.toString());
            }
        } catch (UnAuthorizedAccessException ex) {
            this.logger.log(Level.WARNING, ex.getLocalizedMessage());
            JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());  
        } catch (Exception ex) {
            this.logger.log(Level.WARNING, ex.getLocalizedMessage());
            JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.toString());
        }
    }

    // controller to delete transaction record by id
    @DeleteMapping(path={"/api/transaction/delete"})
    @ResponseBody
    public ResponseEntity<String> deleteTransactionRecordById(@RequestHeader("Jwtauth") String authorizationHeader, @RequestParam Integer transactionId) {

        // check if token valid
        if (!this.jwtTokenUtil.validateJwtToken(authorizationHeader.substring(7))) {
            this.logger.log(Level.INFO, "Invalid jwt token");
            JsonObject error = Json.createObjectBuilder().add("error", "Invalid token").build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }
        // check if username in session
        String username = this.jwtTokenUtil.getUserNameFromJwtToken(authorizationHeader.substring(7));
        if (!this.sessionMap.containsKey(username)) {
            this.logger.log(Level.INFO, "Invalid user session");
            JsonObject error = Json.createObjectBuilder().add("error", "Access denied").build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }

        try {
            if (this.transSvc.deleteTransaction(transactionId)) {
                
                JsonObject success = Json.createObjectBuilder().add("success", "Record deleted").build();
                return ResponseEntity.status(HttpStatus.OK).body(success.toString());
            } else {
                this.logger.log(Level.WARNING, "Record delete failed");
                JsonObject fail = Json.createObjectBuilder().add("fail", "Delete failed").build();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(fail.toString());
            }
        } catch (UnAuthorizedAccessException ex) {
            this.logger.log(Level.WARNING, ex.getLocalizedMessage());
            JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());   
        } catch (Exception ex) {
            this.logger.log(Level.WARNING, ex.getLocalizedMessage());
            JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.toString());
        }
    }

    // controller to retrieve summary - with concurrency rate conversion
    // TODO: to complete
    @GetMapping(path={"/api/transaction/summary/{monthYear}"})
    @ResponseBody
    public ResponseEntity<String> getMonthlySummary(@RequestHeader("Jwtauth") String authorizationHeader, @PathVariable String monthYear) {
        
        // check if token valid
        if (!this.jwtTokenUtil.validateJwtToken(authorizationHeader.substring(7))) {
            this.logger.log(Level.INFO, "Invalid jwt token");
            JsonObject error = Json.createObjectBuilder().add("error", "Invalid token").build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }
        // check if username in session
        String username = this.jwtTokenUtil.getUserNameFromJwtToken(authorizationHeader.substring(7));
        if (!this.sessionMap.containsKey(username)) {
            this.logger.log(Level.INFO, "Invalid user session");
            JsonObject error = Json.createObjectBuilder().add("error", "Access denied").build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }

        String userId = this.sessionMap.get(username).get("userId");
        try {
            Integer month = Integer.valueOf(monthYear.split("_")[0]);
            Integer year = Integer.valueOf(monthYear.split("_")[1]);
            Float[] result = this.transSvc.getSummaries(month, year, userId);
            JsonObject records = this.transSvc.retrieveConvertedTransaction(month, year, userId);
            JsonObject payload = Json.createObjectBuilder().add("income", result[0]).add("spending", result[1]).add("summary", Json.createObjectBuilder(records)).build();
            return ResponseEntity.status(HttpStatus.OK).body(payload.toString());

        } catch (Exception ex) {
            this.logger.log(Level.WARNING, ex.getLocalizedMessage());
            JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error.toString());
        }
    }

    // controller to insert event to google calendar
    // and send email notification
    @PostMapping(path={"/api/notification/create"})
    @ResponseBody
    public ResponseEntity<String> createNotification(@RequestHeader("Jwtauth") String authorizationHeader, @RequestHeader("Authorization") String gToken ,@RequestBody String notification) {

        // check if token valid
        if (!this.jwtTokenUtil.validateJwtToken(authorizationHeader.substring(7))) {
            this.logger.log(Level.INFO, "Invalid jwt token");
            JsonObject error = Json.createObjectBuilder().add("error", "Invalid token").build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }
        // check if username in session
        String username = this.jwtTokenUtil.getUserNameFromJwtToken(authorizationHeader.substring(7));
        if (!this.sessionMap.containsKey(username)) {
            this.logger.log(Level.INFO, "Invalid user session");
            JsonObject error = Json.createObjectBuilder().add("error", "Access denied").build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }

        JsonObject jEvent = Json.createReader(new StringReader(notification)).readObject();
        try {
            // String messageContent = this.gapiSvc.postCalendarEvent(jEvent);
            // Message message = this.gapiSvc.sendEmail(messageContent, jEvent.getString("email"));
            // JsonObject success = Json.createObjectBuilder().add("success", "Notification set, id=%s".formatted(message.getId())).build();

            // TODO: insert transaction for recurring events
            String userId = this.sessionMap.get(username).get("userId");
            System.out.println(">>> sample event: " + jEvent.toString());
            this.transSvc.insertRecurrenceTransaction(jEvent, userId);

            String message = this.gapiSvc.createCalendarEvent(jEvent, gToken.substring(7));
            String notificaiton = this.gapiSvc.postEmailNotification(message, jEvent.getString("email"), gToken.substring(7));

            JsonObject success = Json.createObjectBuilder().add("success", notification).build();
            return ResponseEntity.status(HttpStatus.OK).body(success.toString());
        } catch (Exception ex) {
            this.logger.log(Level.WARNING, ex.getLocalizedMessage());
            JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error.toString());
        }

    }

    // ############## Getting Google OAuth2 Authentication ##################
    // @GetMapping(path={"/googleapi/calendar"})
    // @ResponseBody
    // public ResponseEntity<String> authenticateGoogleCalendar() throws GeneralSecurityException, IOException {

    //     try {
    //         this.gapiSvc.getGoogleApiService("calendar");

    //         JsonObject success = Json.createObjectBuilder().add("success", "Authentication success").build();
    //         return ResponseEntity.status(HttpStatus.OK).body(success.toString());
    //     } catch (Exception ex) {
    //         JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();
    //         return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error.toString());
    //     }
        
        
    // }

    // @GetMapping(path={"/googleapi/gmail"})
    // @ResponseBody
    // public ResponseEntity<String> authenticateGoogleGmail() {

    //     try {
    //         this.gapiSvc.getGoogleApiService("email");

    //         JsonObject success = Json.createObjectBuilder().add("success", "Authentication success").build();
    //         return ResponseEntity.status(HttpStatus.OK).body(success.toString());
    //     } catch (Exception ex) {
    //         JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();
    //         return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error.toString());
    //     }
        
    // }

    // ############## Testing Purpose ######################
    // controller to convert the currency 
    @GetMapping(path={"/api/transaction/converter"})
    @ResponseBody
    public ResponseEntity<String> convertToSGD(@RequestBody String curr) {

        JsonObject req = Json.createReader(new StringReader(curr)).readObject();
        String fromCurrency = req.getString("from");

        try {   
            Float rate = this.currCnvSvc.convertToSGD(fromCurrency);
            JsonObject payload = Json.createObjectBuilder().add("%s_SGD".formatted(fromCurrency), rate).build();

            return ResponseEntity.status(HttpStatus.OK).body(payload.toString());

        } catch (Exception ex) {
            this.logger.log(Level.WARNING, ex.getLocalizedMessage());
            JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error.toString());
        }

    }

}
