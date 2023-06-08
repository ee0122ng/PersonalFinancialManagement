package iss.ibf.pfm_expenses_server.service;

import java.io.StringReader;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import iss.ibf.pfm_expenses_server.model.Account;
import iss.ibf.pfm_expenses_server.model.User;
import iss.ibf.pfm_expenses_server.model.UserDetails;
import iss.ibf.pfm_expenses_server.repository.AccountAuthenticationRepository;
import iss.ibf.pfm_expenses_server.repository.AccountRegistrationRepository;
import iss.ibf.pfm_expenses_server.repository.ProfileCompletionRepository;
import iss.ibf.pfm_expenses_server.repository.ProfileRetrievalRepository;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

@Service
public class AccountService {

    @Autowired
    private AccountRegistrationRepository accRegisterRepo;

    @Autowired
    private AccountAuthenticationRepository accAuthRepo;

    @Autowired
    private ProfileCompletionRepository accCpltRepo;

    @Autowired
    private ProfileRetrievalRepository profileRetRepo;

    public Optional<String> registerUserAccount(User user, String pwd, String email) throws NoSuchAlgorithmException, InvalidKeySpecException {

        String accountId = accRegisterRepo.createUserAccount(user, pwd, email);
        
        return Optional.of(accountId);
    }

    //convert the form received from frontend in Json String to Json Object 
    public JsonObject convertStringToJsonObject(String form) {

        JsonReader jReader = Json.createReader(new StringReader(form));
        JsonObject json = jReader.readObject();

        return json;
    }

    //check if username valid
    public Boolean checkUsername(String username) {
        return this.accRegisterRepo.verifyIfUsernameExist(username) && this.accAuthRepo.verifyIfAccountValid(username);
    }

    //verify if password valid
    public Boolean checkPassword(String username, String pwd) {
        return this.accAuthRepo.verifyIfPasswordValid(username, pwd);
    }

    //check account completion status
    public Boolean checkAccountCompletion(String username) {
        return this.accCpltRepo.verifyAccountCompletionStatus(username);
    }

    //get account
    public Account getUserAccount(String username) {
        Optional<String> opt = this.accCpltRepo.getUserIdByUsername(username);
        String userId = opt.get();

        return this.accAuthRepo.getAccount(userId);
    }

    //complete user info
    public Boolean completeUserAccount(JsonObject userInfoForm) throws ParseException {

        return this.accCpltRepo.updateUserDetails(userInfoForm);
    }

    //get email
    public String getUserEmail(String username) {
        String email = this.accCpltRepo.getUserEmailByUsername(username);

        return email;
    }

    //retrieve profile
    public UserDetails getUserProfile(String username) {

        return this.profileRetRepo.retrieveUserProfile(username);
    }
    
}
