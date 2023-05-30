package iss.ibf.pfm_expenses_server.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import iss.ibf.pfm_expenses_server.exception.AccountCreationException;
import iss.ibf.pfm_expenses_server.model.User;
import iss.ibf.pfm_expenses_server.service.AccountActivationService;
import jakarta.json.Json;
import jakarta.json.JsonObject;

@Controller
@RequestMapping(path={"/api/account"})
public class AccountController {

    @Autowired
    private AccountActivationService accActivateSvc;
    
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> createUserAccount(String form) {

        JsonObject json = accActivateSvc.convertStringToJsonObject(form);

        try {
            User user = new User(json.getString("username"));
            String pwd = json.getString("password");

            Optional<String> opt = accActivateSvc.activateUserAccount(user, pwd);

            try {
                
                String accId = opt.get();
                JsonObject payload = Json.createObjectBuilder().add("payload", accId).build();

                return ResponseEntity.status(HttpStatus.OK).body(payload.toString());

            } catch (Exception ex) {

                JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.toString());

            }

        } catch (Exception ex) {

            JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.toString());
        }
        
    }
}
