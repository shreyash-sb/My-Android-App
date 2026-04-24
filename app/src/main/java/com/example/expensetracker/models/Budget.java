package com.example.expensetracker.models;

import java.io.Serializable;

public class Budget implements Serializable {

    private String id;
    private String userId;
    private String monthKey;
    private String monthLabel;
    private double amount;
    private long updatedAt;

    public Budget() {
    }

    public Budget(String id, String userId, String monthKey, String monthLabel, double amount, long updatedAt) {
        this.id = id;
        this.userId = userId;
        this.monthKey = monthKey;
        this.monthLabel = monthLabel;
        this.amount = amount;
        this.updatedAt = updatedAt;
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

    public String getMonthKey() {
        return monthKey;
    }

    public void setMonthKey(String monthKey) {
        this.monthKey = monthKey;
    }

    public String getMonthLabel() {
        return monthLabel;
    }

    public void setMonthLabel(String monthLabel) {
        this.monthLabel = monthLabel;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
