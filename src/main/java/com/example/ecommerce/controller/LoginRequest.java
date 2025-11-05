package com.example.ecommerce.controller;

public class LoginRequest {
    private String username;
    private String password;
    private boolean rememberMe;

    // === 建構子 ===
    public LoginRequest() {}

    // === Getters & Setters ===
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}