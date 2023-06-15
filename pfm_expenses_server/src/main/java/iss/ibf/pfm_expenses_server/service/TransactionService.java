package iss.ibf.pfm_expenses_server.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import iss.ibf.pfm_expenses_server.model.Activity;
import iss.ibf.pfm_expenses_server.repository.TransactionRepository;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transRepo;

    public Boolean insertTransaction(JsonObject records, String userId) throws ParseException {
    
        return this.transRepo.insertTransaction(this.convertJsonToActivity(records, userId));
    }

    public JsonObject retrieveTransaction(String selectedDate, String userId) throws ParseException {

        // note: java.util.Date.toString() will return format that is not acceptable by MySQL
        // note: use LocalDate.toString() instead

        Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(this.getStartDayofMonth(selectedDate));
        LocalDate start = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse(this.getEndDayofMonth(selectedDate));
        LocalDate end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        List<Activity> activities = this.transRepo.retrieveTransaction(start, end, userId);

        return this.convertActivitiesListToJsonObject(activities);
        
    }

    public Boolean updateTransaction(JsonObject record, String userId) throws ParseException {
        return this.transRepo.updateTransaction(this.convertJsonToActivity(record, userId));
    }

    public Boolean deleteTransaction(Integer id) {
        return this.transRepo.deleteTransaction(id);
    }

    public String getEndDayofMonth(String selectedDate) throws ParseException {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(selectedDate);
        LocalDate localD = zonedDateTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        Calendar endCal = Calendar.getInstance();

        // note: calendar month value has 0 = January
        endCal.set(localD.getYear(), localD.getMonthValue() - 1, localD.lengthOfMonth());

        return new SimpleDateFormat("yyyy-MM-dd").format(endCal.getTime());
    }

    public String getStartDayofMonth(String selectedDate) throws ParseException {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(selectedDate);
        LocalDate localD = zonedDateTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        Calendar endCal = Calendar.getInstance();

        // note: calendar month value has 0 = January
        endCal.set(localD.getYear(), localD.getMonthValue() - 1, 1);

        return new SimpleDateFormat("yyyy-MM-dd").format(endCal.getTime());
    }

    public Activity convertJsonToActivity(JsonObject records, String userId) throws ParseException {
        Activity activity = new Activity();
        activity.setUserId(userId);
        activity.setCategory(records.getString("category").toLowerCase());
        activity.setItem(records.getString("item", ""));
        activity.setAmount( (float) records.getJsonNumber("amount").doubleValue());
        activity.setCurrency(records.getString("currency", ""));

        // convert Json string value to java.util.date
        // note: angular date is in UTC, need to change the time zone to local
        if (null != records.getString("transactionDate")) {
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(records.getString("transactionDate"));
            LocalDate localD = zonedDateTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            activity.setItemDate(new SimpleDateFormat("yyyy-MM-dd").parse(localD.toString()));
        }
        
        // insert id if it exists
        try {
            activity.setId(records.getInt("id"));
            
        } catch (Exception ex) {
            // if null, ignore the null value and return without id
        }
        return activity;
    }

    public JsonObject convertActivitiesListToJsonObject(List<Activity> activities) {

        JsonArrayBuilder jArrBld = Json.createArrayBuilder();

        for (Activity a : activities) {
            JsonObjectBuilder bld = Json.createObjectBuilder()
                                        .add("id", a.getId())
                                        .add("category", a.getCategory())
                                        .add("item", a.getItem())
                                        .add("itemDate", a.getItemDate().toString())
                                        .add("amount", a.getAmount())
                                        .add("currency", a.getCurrency());
            jArrBld.add(bld);
        }


        return Json.createObjectBuilder().add("records", jArrBld).build();
    }

    public List<String> getCurrencies() {

        return this.transRepo.getCurrencies();
    }
    
}
