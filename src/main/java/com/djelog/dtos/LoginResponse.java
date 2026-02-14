package com.djelog.dtos;

import java.util.UUID;

public class LoginResponse {

    private boolean success;
    private String token;
    private String message;
    private String email;
    private String userName;
    private UUID userId;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LoginResponse(boolean success, String token, String message, String email, String userName, UUID userId) {
        this.success = success;
        this.token = token;
        this.message = message;
        this.userName = userName;
        this.email = email;
        this.userId = userId;
    }

    public boolean isSuccess() {
        return success;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
