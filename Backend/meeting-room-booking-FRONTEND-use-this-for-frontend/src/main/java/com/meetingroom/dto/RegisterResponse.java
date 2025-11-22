package com.meetingroom.dto;

public class RegisterResponse {
    private String userId;
    private String email;
    private String message;

    public RegisterResponse(String userId, String email, String message) {
        this.userId = userId;
        this.email = email;
        this.message = message;
    }

    public String getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getMessage() { return message; }
}
