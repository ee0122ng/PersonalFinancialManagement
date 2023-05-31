package iss.ibf.pfm_expenses_server.service;

import java.io.StringReader;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import iss.ibf.pfm_expenses_server.model.Account;
import iss.ibf.pfm_expenses_server.model.User;
import iss.ibf.pfm_expenses_server.repository.AccountAuthenticationRepository;
import iss.ibf.pfm_expenses_server.repository.AccountRegistrationRepository;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

@Service
public class AccountService {

    @Autowired
    private AccountRegistrationRepository accActivateRepo;

    @Autowired
    private AccountAuthenticationRepository accAuthRepo;

    public Optional<String> activateUserAccount(User user, String pwd, String email) throws NoSuchAlgorithmException, InvalidKeySpecException {

        String accountId = accActivateRepo.createUserAccount(user, pwd, email);
        
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
        return this.accActivateRepo.verifyIfUsernameExist(username) && this.accAuthRepo.verifyIfAccountValid(username);
    }

    //verify if password valid
    public Boolean checkPassword(String username, String pwd) {
        return this.accAuthRepo.verifyIfPasswordValid(username, pwd);
    }
    
}
