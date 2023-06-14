package iss.ibf.pfm_expenses_server.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class CurrencyRate implements Serializable {
    
    private Double rate;
    private LocalDateTime lastUpdate;

    public CurrencyRate() {
    }

    public Double getRate() {
        return this.rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public LocalDateTime getLastUpdate() {
        return this.lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Long getHourSinceLastUpdate() {

        return ChronoUnit.HOURS.between(this.lastUpdate.toLocalTime(), LocalDateTime.now().toLocalTime());
    }

}
