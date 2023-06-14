package iss.ibf.pfm_expenses_server.repository;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import iss.ibf.pfm_expenses_server.exception.CurrencyConverterException;
import iss.ibf.pfm_expenses_server.service.CurrencyConverterService;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

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

    private final String GET_CURRENCIES_API_URL = "https://free.currconv.com/api/v7/currencies";

    private List<String> CURRENCIES = new ArrayList<String>();

    public Boolean insertTransaction(JsonObject records, String userId) {

        Integer updatedResult = jdbcTemplate.update(INSERT_NEW_RECORD_SQL, userId, records.getString("category", ""), records.getString("item", ""), records.get("transactionDate"), records.getJsonNumber("amount").doubleValue(), records.getString("currency"));

        return updatedResult > 0;
    }

    public List<String> getCurrencies() {

        if (null == redisTemplate.opsForHash().get("currencyStore", "currencyList")) {

             System.out.println(">>> api currency list: " + CURRENCIES);

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

        System.out.println(">>> currency list: " + CURRENCIES);

        return CURRENCIES;
    }

    public Double getConversionRate(String fromCurrency) {

        return fromCurrency.toUpperCase().equals("SGD") ? 1d : this.currCnvSvc.convertToSGD(fromCurrency);
    }
    
}
