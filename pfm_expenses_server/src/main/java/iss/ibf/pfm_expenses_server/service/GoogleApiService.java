package iss.ibf.pfm_expenses_server.service;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;

import iss.ibf.pfm_expenses_server.exception.EmailSendingFailException;
import jakarta.json.JsonObject;

@Service
public class GoogleApiService {
    
    private static final String APPLICATION_NAME="SoonHang Pfm Google Services";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIR_PATH_CALENDAR = "tokens/calendar"; // directory to store authorization tokens for this application
    private static final String TOKENS_DIR_PATH_GMAIL = "tokens/gmail";
    private static final String CREDENTIALS_FILE_PATH = "/Google Workspace Pfm.json";

    private static final List<String> CALENDAR_SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final List<String> GMAIL_SCOPES = Collections.singletonList(GmailScopes.GMAIL_SEND);

    private static Calendar googleCalendar;
    private static Gmail googleMail;

    private static Credential googleCalendarCredential;
    private static Credential googleGmailCredential;

    private static final String GMAIL_ADDRESS="ee0003ng@gmail.com";

    public Object getGoogleApiService(String type) throws GeneralSecurityException, IOException {

        //Build a new authorized API client service
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Object service = new Object();

        switch (type.toLowerCase()) {
            case "calendar":
                Calendar cal = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCalendarCredentials(HTTP_TRANSPORT))
                                    .setApplicationName(APPLICATION_NAME)
                                    .build();
                service = cal;
                this.googleCalendar = cal;
            case "email":
                Gmail mail = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getGmailCredentials(HTTP_TRANSPORT))
                                    .setApplicationName(APPLICATION_NAME)
                                    .build();
                service = mail;
                this.googleMail = mail;
                    
        }

        return Optional.of(service).orElseThrow(()->new RuntimeException("No service selected"));

    }

    public Message sendEmail(String body, String toEmailAddress) throws MessagingException, IOException {
        
        try {
            // create email
            MimeMessage email = this.createEmail(body, toEmailAddress);

            // wrap the MIME message into a gmail messsage
            Message message = this.createMessageWithEmail(email);

            // create send message
            message = googleMail.users().messages().send("me", message).execute();
            return message;
        } catch (GoogleJsonResponseException e) {
            GoogleJsonError error = e.getDetails();
            if (error.getCode() == 403) {
                throw new EmailSendingFailException("Unable to send message: " + e.getMessage());
            } else {
                throw e;
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    private MimeMessage createEmail(String body, String toEmailAddress) throws MessagingException{

        Properties prop = new Properties();
        Session session = Session.getDefaultInstance(prop);

        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(GMAIL_ADDRESS));
        email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(toEmailAddress));
        email.setSubject("Notification from PFM");
        email.setText(body);
        return email;
    }
    
    private Message createMessageWithEmail(MimeMessage emailContent) throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        emailContent.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }

    public String postCalendarEvent(JsonObject content) throws Exception {

        // create calendar event object
        Event event = this.createEventObject(content);
        event = googleCalendar.events().insert("primary", event).execute();

        // return text to be send as email content
        /*
         * Event created: <Summary>
         * Recurrence: Yes, <Type>
         */
        String summary = event.getSummary();
        List<String> recurrenceType = event.getRecurrence();
        String recurrence = "";
        if (recurrenceType.size() > 0) {
            recurrence = recurrenceType.get(0).split("=")[1].split(";")[0];
        }

        return recurrence.isEmpty() ? "Event created: %s\nRecurrence: No" : "Event created: %s\nRecurrence: Yes, %s".formatted(event.getSummary(), recurrence);
    }

    public Event createEventObject(JsonObject content) {

        Event event = new Event();
        // set default setting: Description, Summary, Created Date, Recurrence
        if (!content.getString("description", "").isEmpty()) {
            event.setDescription(content.getString("description"));
        }

        event.setCreated(new DateTime(System.currentTimeMillis()))
            .setSummary(content.getString("summary"))
            .setRecurrence(Arrays.asList(new String[] {"RRULE:FREQ=%s;COUNT=%d".formatted(content.getString("frequency").toUpperCase(), content.getInt("count"))}));
        
        // set event start datetime (required)
        String sDate = content.getString("startDate");
        ZonedDateTime zdtStart = ZonedDateTime.parse(sDate);
        DateTime startDateTime = new DateTime(zdtStart.toInstant().toEpochMilli());
        EventDateTime start = new EventDateTime()
            .setDateTime(startDateTime)
            .setTimeZone("Asia/Singapore");
        event.setStart(start);

        // set event end datetime (required) - 15 minutes after the start
        DateTime endDateTime = new DateTime(zdtStart.toInstant().toEpochMilli() + 15*60*1000L);
        EventDateTime end = new EventDateTime()
            .setDateTime(endDateTime)
            .setTimeZone("Asia/Singapore");
        event.setEnd(end);
        
        // set event reminder by email - 3 days in advance before next charges
        EventReminder[] reminderOverrides = new EventReminder[] {
            new EventReminder().setMethod("email").setMinutes(3 * 24 * 60)
        };
        Event.Reminders reminders = new Event.Reminders()
                                                .setUseDefault(false)
                                                .setOverrides(Arrays.asList(reminderOverrides));
        event.setReminders(reminders);

        return event;
    }

    // create an authorized credential object: Google Calendar
    private static Credential getCalendarCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {

        // get credential from local directory
        InputStream in = GoogleApiService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);

        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }

        // retrieve ClientSecret from the local file - Google Workspace Pfm.json
        GoogleClientSecrets  clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow
            .Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, CALENDAR_SCOPES)
            .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIR_PATH_CALENDAR)))
            .setApprovalPrompt("force")
            .setAccessType("offline")
            .build();

        // direct the return to temporary port for auth token extraction
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8887).build();
        
        // create credential that will expire in 10 minutes
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user").setExpiresInSeconds(10*60L);
        googleCalendarCredential = credential;

        // kill the listening port
        receiver.stop();

        // returns an authorized Credential object
        return credential;
    }

    // create an authorized credential object: Google Gmail
    private static Credential getGmailCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {

        // get credential from local directory
        InputStream in = GoogleApiService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);

        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }

        // retrieve ClientSecret from the local file - Google Workspace Pfm.json
        GoogleClientSecrets  clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow
            .Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, GMAIL_SCOPES)
            .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIR_PATH_GMAIL)))
            .setApprovalPrompt("force")
            .setAccessType("offline")
            .build();

        // direct the return to temporary port for auth token extraction
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        
        // create credential that will expire in 10 minutes
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user").setExpiresInSeconds(10*60L);
        googleGmailCredential = credential;

        // kill the listening port
        receiver.stop();

        // returns an authorized Credential object
        return credential;
    }

}
