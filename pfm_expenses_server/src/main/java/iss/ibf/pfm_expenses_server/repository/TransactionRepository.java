package iss.ibf.pfm_expenses_server.repository;

import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import iss.ibf.pfm_expenses_server.exception.ActivityTableException;
import iss.ibf.pfm_expenses_server.exception.CurrencyConverterException;
import iss.ibf.pfm_expenses_server.model.Activity;
import iss.ibf.pfm_expenses_server.service.CurrencyConverterService;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.transaction.Transactional;

@Repository
public class TransactionRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CurrencyConverterService currCnvSvc;

    @Value("${currency.converter.apikey}")
    private String APIKEY;

    private final String INSERT_NEW_RECORD_SQL = "insert into activities(user_id, category, item, item_date, amount, currency) values(?, ? ,? , ?, ?, ?)";
    private final String RETRIEVE_RECORDS_SQL = "select * from activities where (user_id=?) and (item_date between ? and ?)";
    private final String UPDATE_RECORDS_SQL = "update activities set category=?, item=?, item_date=?, amount=?, currency=? where id=?";
    private final String DELETE_RECORDS_SQL = "delete from activities where id=?";

    private final String GET_CURRENCIES_API_URL = "https://free.currconv.com/api/v7/currencies";

    private List<String> CURRENCIES = new ArrayList<String>();

    public Boolean insertTransaction(Activity activity) throws ParseException {

        Integer insertedResult = jdbcTemplate.update(INSERT_NEW_RECORD_SQL, activity.getUserId(), 
                                                        activity.getCategory(), activity.getItem(), activity.getItemDate(), activity.getAmount(), activity.getCurrency());

        return insertedResult > 0;
    }

    public List<Activity> retrieveTransaction(LocalDate startDate, LocalDate endDate, String userId) {

        try {
            List<Activity> transactions = jdbcTemplate.query(RETRIEVE_RECORDS_SQL, BeanPropertyRowMapper.newInstance(Activity.class), userId, startDate.toString(), endDate.toString());
            return transactions;
        } catch(Exception ex) {
            throw new ActivityTableException("No transaction record found");
        }

    }

    public List<Activity> retrieveConvertedTransaction(LocalDate startDate, LocalDate endDate, String userId) {

        try {
            List<Activity> transactions = jdbcTemplate.query(RETRIEVE_RECORDS_SQL, BeanPropertyRowMapper.newInstance(Activity.class), userId, startDate.toString(), endDate.toString());
            List<Float> convertedAmount = transactions.stream().map(t -> t.getAmount() * this.getConversionRate(t.getCurrency())).toList();

            for (int i = 0; i < convertedAmount.size(); i++) {
                transactions.get(i).setAmount(convertedAmount.get(i));
            }

            return transactions;
        } catch(Exception ex) {
            throw new ActivityTableException("No transaction record found");
        }

    }

    public Boolean updateTransaction(Activity activity) throws ParseException {

        Integer updatedResult = jdbcTemplate.update(UPDATE_RECORDS_SQL, activity.getCategory(), activity.getItem(), activity.getItemDate(), activity.getAmount(), activity.getCurrency(), activity.getId());
        
        return updatedResult > 0;
    }
    
    public Boolean deleteTransaction(Integer transactionId) {

        Integer deletedResult = jdbcTemplate.update(DELETE_RECORDS_SQL, transactionId);

        return deletedResult > 0;
    }

    // TODO: to complete
    public Float[] getSummaries(LocalDate startDate, LocalDate endDate, String userId) {
        try {
            // get list of transaction based on month requested
            List<Activity> transactions = this.retrieveTransaction(startDate, endDate, userId);

            Float incomes = 0f;
            Float spendings = 0f;

            // convert all currency to SGD base
            if (transactions.size() > 0) {
                List<Activity> incomeList = transactions.stream().filter(t -> t.getCategory().equals("income")).toList();
                List<Activity> spendingList = transactions.stream().filter(t -> t.getCategory().equals("spending")).toList();

                // convert both list to SGD
                if (incomeList.size() > 0) {
                    incomes = incomeList.stream().map(t -> (float) t.getAmount() * (float) this.getConversionRate(t.getCurrency())).reduce(0f, (a,b) -> (a+b));
                }
                if (spendingList.size() > 0) {
                    spendings = spendingList.stream().map(t -> (float) t.getAmount() * (float) this.getConversionRate(t.getCurrency())).reduce(0f, (a,b) -> (a+b));
                }
            }

            return new Float[] {incomes, spendings};

        } catch (Exception ex) {
            throw ex;
        }
        
    }

    public List<String> getCurrencies() {

        if (null == redisTemplate.opsForHash().get("currencyStore", "currencyList")) {

            final String URL = UriComponentsBuilder
                    .fromUriString(GET_CURRENCIES_API_URL)
                    .queryParam("apiKey", APIKEY)
                    .toUriString();
            
            RequestEntity<Void> req = RequestEntity.get(URL).accept().build();

            ResponseEntity<String> rep = new RestTemplate().exchange(URL, HttpMethod.GET, req, String.class);

            if(rep.getStatusCode().value() == 200) {
                String payload = rep.getBody();
                JsonReader jReader = Json.createReader(new StringReader(payload));
                JsonObject result = jReader.readObject();
                JsonObject jCurrency = result.getJsonObject("results");
                this.CURRENCIES =  jCurrency.entrySet().stream().map(s -> s.getValue().asJsonObject().getString("id")).sorted().toList();

                redisTemplate.opsForHash().put("currencyStore", "currencyList", CURRENCIES);

            } else {
                throw new CurrencyConverterException("Failed to retrieve currency list");
            }

        }

        CURRENCIES = (List<String>) redisTemplate.opsForHash().get("currencyStore", "currencyList");

        return CURRENCIES;
    }

    public Float getConversionRate(String fromCurrency) {

        return fromCurrency.toUpperCase().equals("SGD") ? 1f : this.currCnvSvc.convertToSGD(fromCurrency);
    }

    @Transactional
    public Boolean insertDailyRecurrenceTransaction(Activity activity, Integer recurrCount, String userId) throws Exception {
        
        // insert recurrence transactions
        for (int i = 1; i < recurrCount; i++) {
            try {
                Calendar cal = Calendar.getInstance();
                cal.set(activity.getItemDate().toInstant().atZone(ZoneId.systemDefault()).getYear(), activity.getItemDate().toInstant().atZone(ZoneId.systemDefault()).getMonthValue() - 1, activity.getItemDate().toInstant().atZone(ZoneId.systemDefault()).getDayOfMonth());
                cal.add(Calendar.DAY_OF_MONTH, 1);
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                String toInsertDate = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());

                activity.setItemDate(formatter.parse(toInsertDate));
                this.insertTransaction(activity);

            } catch (Exception ex) {
                throw ex;
            }
        }

        return true;
        
    }

    @Transactional
    public Boolean insertWeeklyRecurrenceTransaction(Activity activity, Integer recurrCount, String userId) throws Exception {

        // insert recurrence transactions
        for (int i = 1; i < recurrCount; i++) {
            try {
                Calendar cal = Calendar.getInstance();
                cal.set(activity.getItemDate().toInstant().atZone(ZoneId.systemDefault()).getYear(), activity.getItemDate().toInstant().atZone(ZoneId.systemDefault()).getMonthValue() - 1, activity.getItemDate().toInstant().atZone(ZoneId.systemDefault()).getDayOfMonth());
                cal.add(Calendar.DAY_OF_MONTH, 7);
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                String toInsertDate = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());

                activity.setItemDate(formatter.parse(toInsertDate));
                Boolean result = this.insertTransaction(activity);

            } catch (Exception ex) {
                throw ex;
            }
        }

        return true;
        
    }

    @Transactional
    public Boolean insertMonthlyRecurrenceTransaction(Activity activity, Integer recurrCount, String userId) throws Exception {

        // insert recurrence transactions
        for (int i = 1; i < recurrCount; i++) {
            try {
                Calendar cal = Calendar.getInstance();
                cal.set(activity.getItemDate().toInstant().atZone(ZoneId.systemDefault()).getYear(), activity.getItemDate().toInstant().atZone(ZoneId.systemDefault()).getMonthValue() - 1, activity.getItemDate().toInstant().atZone(ZoneId.systemDefault()).getDayOfMonth());
                cal.add(Calendar.MONTH, 1);
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                String toInsertDate = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());

                activity.setItemDate(formatter.parse(toInsertDate));
                this.insertTransaction(activity);

            } catch (Exception ex) {
                throw ex;
            }
        }

        return true;
        
    }

    @Transactional
    public Boolean insertYearlyRecurrenceTransaction(Activity activity, Integer recurrCount, String userId) throws Exception {
        
        // insert recurrence transactions
        for (int i = 1; i < recurrCount; i++) {
            try {
                Calendar cal = Calendar.getInstance();
                cal.set(activity.getItemDate().toInstant().atZone(ZoneId.systemDefault()).getYear(), activity.getItemDate().toInstant().atZone(ZoneId.systemDefault()).getMonthValue() - 1, activity.getItemDate().toInstant().atZone(ZoneId.systemDefault()).getDayOfMonth());
                cal.add(Calendar.YEAR, 1); // every iteration is a newly updated activity
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                String toInsertDate = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());

                activity.setItemDate(formatter.parse(toInsertDate));
                this.insertTransaction(activity);

            } catch (Exception ex) {
                throw ex;
            }
        }

        return true;
        
    }
    
}
