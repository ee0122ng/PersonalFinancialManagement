package iss.ibf.pfm_expenses_server.repository;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import iss.ibf.pfm_expenses_server.exception.CurrencyConverterException;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

@Repository
public class TransactionRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${currency.converter.apikey}")
    private String APIKEY;

    private final String GET_CURRENCIES_API_URL = "https://free.currconv.com/api/v7/currencies";
    private final String GET_CONVERTER_API_URL = "https://free.currconv.com/api/v7/convert";

    private List<String> CURRENCIES = new ArrayList<String>();

    public List<String> getCurrencies() {
        
        // call convert currency api if CURRENCIES list not yet exists
        if (null == CURRENCIES || CURRENCIES.size() <= 0) {

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

            } else {
                throw new CurrencyConverterException("Failed to retrieve currency list");
            }

        }

        return CURRENCIES;
    }
    
}
