package com.example.expensetracker.models;

import java.io.Serializable;

public class Expense implements Serializable {

    private String id;
    private String userId;
    private double amount;
    private String category;
    private String note;
    private long date;
    private String imageUrl;
    private String paymentSource;

    public Expense() {
    }

    public Expense(String id, String userId, double amount, String category, String note, long date, String imageUrl, String paymentSource) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.category = category;
        this.note = note;
        this.date = date;
        this.imageUrl = imageUrl;
        this.paymentSource = paymentSource;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPaymentSource() {
        return paymentSource;
    }

    public void setPaymentSource(String paymentSource) {
        this.paymentSource = paymentSource;
    }
}
