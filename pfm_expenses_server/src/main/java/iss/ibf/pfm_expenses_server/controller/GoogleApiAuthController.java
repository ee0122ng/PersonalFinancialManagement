package iss.ibf.pfm_expenses_server.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import iss.ibf.pfm_expenses_server.model.GoogleAuthToken;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class GoogleApiAuthController {

    private RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.security.oauth2.client.registration.Google.client-id}")
    private String CLIENT_ID;

    @Value("${spring.security.oauth2.client.registration.Google.client-secret}")
    private String CLIENT_SECRET;

    public static final String GOOGLE_AUTH_ENDPOINT="https://accounts.google.com/o/oauth2/v2/auth";
    public static final String GOOGLE_TOKEN_ENDPOINT="https://oauth2.googleapis.com/token";

    // likely need to amend
    public static final String REDIRECT_URL="http://localhost:4200/callback";

    public static final String GOOGLE_SCOPES="https://www.googleapis.com/auth/gmail.send+https://www.googleapis.com/auth/calendar";

    public static final String GOOGLE_GET_CAL_EVENT_ENDPOINT="https://www.googleapis.com/calendar/v3/users/me/calendarList/primary";
    public static final String GOOGLE_INSERT_CAL_EVENT_ENDPOINT="https://www.googleapis.com/calendar/v3/calendars/primary/events";
    public static final String GOOGLE_SEND_EMAIL_ENDPOINT="https://gmail.googleapis.com/upload/gmail/v1/users/me/messages/send";

    public static String AUTH_CODE = "";
    public static String ACCESS_TOKEN = "";
    public static Integer EXPIRES_IN = 0;
    public static Date TOKEN_EXPIRES_TIME;

    // step1: use client id to obtain an authorization code
    @GetMapping({"/api/googleapi/access"})
    public ResponseEntity<String> accessGoogle() {

        System.out.println(">>> invoked...");

        try {
            if(ACCESS_TOKEN.length() > 0) {
                JsonObject success = Json.createObjectBuilder()
                            .add("token", ACCESS_TOKEN)
                            .add("expires_in", EXPIRES_IN)
                            .build();
                return ResponseEntity.status(HttpStatus.OK).body(success.toString());
            } else {
                throw new RuntimeException("No token generated");
            }
        } catch (Exception ex) {
            JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error.toString());
        }

        // try {
        //     String URL = UriComponentsBuilder.fromUriString(GOOGLE_AUTH_ENDPOINT)
        //                 .queryParam("client_id", CLIENT_ID)
        //                 .queryParam("redirect_uri", REDIRECT_URL)
        //                 .queryParam("response_type", "code")
        //                 .queryParam("scope", GOOGLE_SCOPES)
        //                 .queryParam("access_type", "offline")
        //                 .toUriString();

        //     RequestEntity<Void> req = RequestEntity.post(URL).build();

        //     ResponseEntity<String> rep = restTemplate.exchange(URL, HttpMethod.POST, req, String.class);

        //     JsonObject success = Json.createObjectBuilder()
        //                                 .add("token", ACCESS_TOKEN)
        //                                 .add("expires_in", EXPIRES_IN)
        //                                 .build();
        //     return ResponseEntity.status(HttpStatus.OK).body(success.toString());

        // } catch (Exception ex) {
        //     JsonObject error = Json.createObjectBuilder().add("error", ex.getMessage()).build();
        //     return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error.toString());
        // }

    }

    // setp2: use authorization code to generate an access token
    @GetMapping({"/google/code"})
    @ResponseBody
    public String returnAuthCode(HttpServletRequest r) {
        
        System.out.printf(">>> sample request: " + r.getParameterNames());
        System.out.printf(">>> sample parameters: " + r.getQueryString());
        if (r.getParameter("access_token") != null) {
            System.out.printf(">>> auth code: %s\n".formatted(r.getParameter("code")));
            AUTH_CODE = String.valueOf(r.getParameter("code"));

            // set body content
            MultiValueMap<String, String> content = new LinkedMultiValueMap<String, String>();
            content.add("code", AUTH_CODE);
            content.add("client_id", CLIENT_ID);
            content.add("client_secret", CLIENT_SECRET);
            content.add("redirect_uri", REDIRECT_URL);
            content.add("grant_type", "authorization_code");

            // create request entity
            RequestEntity<MultiValueMap<String, String>> req = RequestEntity.post(GOOGLE_TOKEN_ENDPOINT)
                                                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                                                .body(content);
            
            ResponseEntity<GoogleAuthToken> rep = restTemplate.exchange(GOOGLE_TOKEN_ENDPOINT, HttpMethod.POST, req, GoogleAuthToken.class);

            GoogleAuthToken token = rep.getBody();
            this.returnAuthToken(token);

            return "Access authenticated";

        } else {
            // throw new RuntimeException("Calling google api failed");
            return "Calling google api failed";
        }


    }

    @GetMapping({"/token/expire"})
    public String checkIsTokenExpired(String authCode) throws InterruptedException {

        return TOKEN_EXPIRES_TIME == null ? "Token invalid" : "expired=%b, valid till=%s".formatted(TOKEN_EXPIRES_TIME.getTime() <= new Date().getTime(), TOKEN_EXPIRES_TIME.toString());
    }

    public GoogleAuthToken returnAuthToken (GoogleAuthToken token) {


        ACCESS_TOKEN = token.getAccess_token();
        EXPIRES_IN = token.getExpires_in();

        // set token expired time
        Date currDate = new Date();
        Calendar cal = new GregorianCalendar();
        cal.setTime(currDate);
        cal.add(Calendar.SECOND, token.getExpires_in());
        TOKEN_EXPIRES_TIME = cal.getTime();

        System.out.printf(">>> access token:%s\n".formatted(ACCESS_TOKEN));
        System.out.printf(">>> expires in:%d\n".formatted(EXPIRES_IN));

        GoogleAuthToken googleToken = new GoogleAuthToken();
        googleToken.setAccess_token(ACCESS_TOKEN);
        googleToken.setExpires_in(EXPIRES_IN);

        return googleToken;

    }
    
}
