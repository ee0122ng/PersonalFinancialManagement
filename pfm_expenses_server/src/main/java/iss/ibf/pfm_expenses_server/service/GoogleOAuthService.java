// package iss.ibf.pfm_expenses_server.service;

// import java.util.Calendar;
// import java.util.Date;
// import java.util.GregorianCalendar;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.http.HttpMethod;
// import org.springframework.http.MediaType;
// import org.springframework.http.RequestEntity;
// import org.springframework.http.ResponseEntity;
// import org.springframework.stereotype.Service;
// import org.springframework.util.LinkedMultiValueMap;
// import org.springframework.util.MultiValueMap;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.util.UriComponentsBuilder;

// import jakarta.servlet.http.HttpServletRequest;

// @Service
// public class GoogleOAuthService {

//     @Value("${spring.security.oauth2.client.registration.Google.client-id}")
//     private String CLIENT_ID;

//     @Value("${spring.security.oauth2.client.registration.Google.client-secret}")
//     private String CLIENT_SECRET;

//     public static final String GOOGLE_AUTH_ENDPOINT="https://accounts.google.com/o/oauth2/v2/auth";
//     public static final String GOOGLE_TOKEN_ENDPOINT="https://oauth2.googleapis.com/token";

//     public static final String REDIRECT_URL="http://localhost:8080/google/code";
//     public static final String REDIRECT_URL_2="http://localhost:8080/google/token";

//     public static final String GOOGLE_SCOPES="https://www.googleapis.com/auth/gmail.send+https://www.googleapis.com/auth/calendar";
//     public static final String GOOGLE_EMAIL_SCOPE="https://www.googleapis.com/auth/gmail.send";
//     public static final String GOOGLE_CALENDARLIST_SCOPE="https://www.googleapis.com/auth/calendar";

//     public static final String GOOGLE_GET_CAL_EVENT_ENDPOINT="https://www.googleapis.com/calendar/v3/users/me/calendarList/primary";
//     public static final String GOOGLE_INSERT_CAL_EVENT_ENDPOINT="https://www.googleapis.com/calendar/v3/calendars/primary/events";
//     public static final String GOOGLE_SEND_EMAIL_ENDPOINT="https://gmail.googleapis.com/upload/gmail/v1/users/me/messages/send";

//     // step1: use client id to obtain an authorization code
//     public String accessGoogle() {

//         String URL = UriComponentsBuilder.fromUriString(GOOGLE_AUTH_ENDPOINT)
//                                 .queryParam("client_id", CLIENT_ID)
//                                 .queryParam("redirect_uri", REDIRECT_URL)
//                                 .queryParam("response_type", "code")
//                                 .queryParam("scope", GOOGLE_SCOPES)
//                                 .queryParam("access_type", "offline")
//                                 .toUriString();
        
//         // System.out.println(">>> constructed url: " + URL);

//         RequestEntity<Void> req = RequestEntity.post(URL).build();

//         ResponseEntity<String> rep = restTemplate.exchange(URL, HttpMethod.POST, req, String.class);

//         // System.out.println(">>> trigger valid: " + rep.getBody());

//         // to calendar 
//         // System.out.println(">>> response body: " + rep.getBody());
//         // System.out.println(">>> response header: " + rep.getHeaders());
//         // System.out.println(">>> response: " + rep.toString());

//         return rep.toString();

//     }

//     // setp2: use authorization code to generate an access token
//     @GetMapping({"/google/code"})
//     public void returnAuthCode(HttpServletRequest r) {

//         // System.out.println(">>> redirected to authcode storing controller...");

//         if (r.getParameter("code") != null) {
//             System.out.printf(">>> auth code: %s\n".formatted(r.getParameter("code")));
//             AUTH_CODE = String.valueOf(r.getParameter("code"));

//             // set body content
//             MultiValueMap<String, String> content = new LinkedMultiValueMap<String, String>();
//             content.add("code", AUTH_CODE);
//             content.add("client_id", CLIENT_ID);
//             content.add("client_secret", CLIENT_SECRET);
//             content.add("redirect_uri", REDIRECT_URL);
//             content.add("grant_type", "authorization_code");

//             // create request entity
//             RequestEntity<MultiValueMap<String, String>> req = RequestEntity.post(GOOGLE_TOKEN_ENDPOINT)
//                                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED)
//                                                                 .body(content);
            
//             ResponseEntity<GoogleAuthToken> rep = restTemplate.exchange(GOOGLE_TOKEN_ENDPOINT, HttpMethod.POST, req, GoogleAuthToken.class);
//             // System.out.println(">>> response from auth code sent: " + rep.getBody().toString());

//             GoogleAuthToken token = rep.getBody();

//             this.returnAuthToken(token);

//         } else {
//             System.out.printf("error: %s".formatted(r.getParameter("error")));
//         }


//     }

//     public void returnAuthToken (GoogleAuthToken token) {

//         // System.out.println(">>> redirected to access code storing controller..." + token.toString());


//         ACCESS_TOKEN = token.getAccess_token();
//         EXPIRES_IN = (long) token.getExpires_in();

//         // set token expired time
//         Date currDate = new Date();
//         Calendar cal = new GregorianCalendar();
//         cal.setTime(currDate);
//         cal.add(Calendar.SECOND, token.getExpires_in());
//         TOKEN_EXPIRES_TIME = cal.getTime();

//         System.out.printf(">>> access token:%s\n".formatted(ACCESS_TOKEN));
//         System.out.printf(">>> expires in:%d\n".formatted(EXPIRES_IN));

//     }
    
// }
