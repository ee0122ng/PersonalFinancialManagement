package iss.ibf.pfm_expenses_server.service;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import iss.ibf.pfm_expenses_server.exception.CurrencyConverterException;
import iss.ibf.pfm_expenses_server.model.CurrencyRate;
import jakarta.json.Json;

@Service
public class CurrencyConverterService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${currency.converter.apikey}")
    private String APIKEY;

    private final String GET_CONVERTER_API_URL = "https://free.currconv.com/api/v7/convert";
    private Map<String, Double> CONVERTER = new HashMap<String, Double>();

    public Double convertToSGD(String fromCurrency) {

        String key = new String("%s_SGD").formatted(fromCurrency.toUpperCase());
        CurrencyRate rate = new CurrencyRate();

        if (null == redisTemplate.opsForHash().get("converterStore", key)) {

            // call api service to insert new record to redis
            callCurrencyApi(key);
            rate = (CurrencyRate) redisTemplate.opsForHash().get("converterStore", key);

        } else {

            rate = (CurrencyRate) redisTemplate.opsForHash().get("converterStore", key);
            
            if (rate.getHourSinceLastUpdate() >= 1L) {

                // if last update is more than an hour
                callCurrencyApi(key);
                rate = (CurrencyRate) redisTemplate.opsForHash().get("converterStore", key);
            }

        }

        return rate.getRate();
    }

    public void callCurrencyApi(String key) {
        
        final String URL = UriComponentsBuilder
                                .fromUriString(GET_CONVERTER_API_URL)
                                .queryParam("q", key)
                                .queryParam("compact", "ultra")
                                .queryParam("apiKey", APIKEY)
                                .toUriString();
            
        RequestEntity<Void> req = RequestEntity.get(URL).build();
        ResponseEntity<String> rep = new RestTemplate().exchange(URL, HttpMethod.GET, req, String.class);

        if (rep.getStatusCode().value() == 200) {
            String converter = rep.getBody();
            System.out.println(">>> api return: " + converter);
            Double currencyRate = Json.createReader(new StringReader(converter)).readObject().getJsonNumber(key).doubleValue();

            System.out.println(">>> api returned rate " + currencyRate);

            CurrencyRate rate = new CurrencyRate();
            rate.setRate(currencyRate);
            rate.setLastUpdate(LocalDateTime.now());
            
            redisTemplate.opsForHash().put("converterStore", key, rate);

        } else {
            throw new CurrencyConverterException("Failed to retrieve currency rate");
        }

    }
    
}
