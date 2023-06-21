package iss.ibf.pfm_expenses_server.model;

import java.io.Serializable;
import java.util.Date;

import iss.ibf.pfm_expenses_server.validator.Category;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class Activity implements Serializable {

    private Integer id;

    private String userId;

    @Category(message="Invalid category submitted")
    private String category;

    private String item;

    private Date itemDate;

    @NotBlank(message="Amount cannot be blank")
    @Min(value=0, message="Amount cannot be less than 0")
    private Float amount = 0f;

    private String currency;

    public Activity() {
    }

    public Activity(String userId, String category, String item, Date itemDate, Float amount, String currency) {
        this.userId = userId;
        this.category = category;
        this.item = item;
        this.itemDate = itemDate;
        this.amount = amount;
        this.currency = currency;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getItem() {
        return this.item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public Date getItemDate() {
        return this.itemDate;
    }

    public void setItemDate(Date itemDate) {
        this.itemDate = itemDate;
    }

    public Float getAmount() {
        return this.amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public String toString() {
        return "{" +
            " id='" + getId() + "'" +
            ", userId='" + getUserId() + "'" +
            ", category='" + getCategory() + "'" +
            ", item='" + getItem() + "'" +
            ", itemDate='" + getItemDate() + "'" +
            ", amount='" + getAmount() + "'" +
            ", currency='" + getCurrency() + "'" +
            "}";
    }

}
