package com.example.expensetracker.models;

import java.io.Serializable;

public class UserProfile implements Serializable {

    private String userId;
    private String name;
    private String email;
    private String phone;
    private long createdAt;

    public UserProfile() {
    }

    public UserProfile(String userId, String name, String email, String phone, long createdAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
