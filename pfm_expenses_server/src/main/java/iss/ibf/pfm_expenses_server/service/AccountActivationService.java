package iss.ibf.pfm_expenses_server.service;

import java.io.StringReader;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import iss.ibf.pfm_expenses_server.model.User;
import iss.ibf.pfm_expenses_server.repository.AccountActivationRepository;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

@Service
public class AccountActivationService {

    @Autowired
    private AccountActivationRepository accActivateRepo;

    public Optional<String> activateUserAccount(User user, String pwd) throws NoSuchAlgorithmException, InvalidKeySpecException {

        String accountId = accActivateRepo.createUserAccount(user, pwd);
        
        return Optional.of(accountId);
    }

    // code below convert the form received from frontend in Json String to Json Object 
    public JsonObject convertStringToJsonObject(String form) {

        JsonReader jReader = Json.createReader(new StringReader(form));
        JsonObject json = jReader.readObject();

        return json;
    }
    
}
