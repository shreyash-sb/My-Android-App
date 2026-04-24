package com.example.expensetracker.models;

import java.io.Serializable;

public class Income implements Serializable {

    private String id;
    private String userId;
    private double amount;
    private String reason;
    private long date;

    public Income() {
    }

    public Income(String id, String userId, double amount, String reason, long date) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.reason = reason;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
