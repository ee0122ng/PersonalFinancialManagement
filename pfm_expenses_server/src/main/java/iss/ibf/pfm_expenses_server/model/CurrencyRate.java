package iss.ibf.pfm_expenses_server.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class CurrencyRate implements Serializable {
    
    private Float rate;
    private LocalDateTime lastUpdate;

    public CurrencyRate() {
    }

    public Float getRate() {
        return this.rate;
    }

    public void setRate(Float rate) {
        this.rate = rate;
    }

    public LocalDateTime getLastUpdate() {
        return this.lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Long getHourSinceLastUpdate() {

        return ChronoUnit.HOURS.between(getLastUpdate(), LocalDateTime.now());
    }

}
