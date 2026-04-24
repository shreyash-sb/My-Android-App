package com.example.expensetracker.models;

import java.io.Serializable;

public class SavingsGoal implements Serializable {

    private String id;
    private String userId;
    private String monthKey;
    private String monthLabel;
    private double targetAmount;
    private String note;
    private long updatedAt;

    public SavingsGoal() {
    }

    public SavingsGoal(String id, String userId, String monthKey, String monthLabel,
                       double targetAmount, String note, long updatedAt) {
        this.id = id;
        this.userId = userId;
        this.monthKey = monthKey;
        this.monthLabel = monthLabel;
        this.targetAmount = targetAmount;
        this.note = note;
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

    public double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
