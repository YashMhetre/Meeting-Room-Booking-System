package com.meetingroom.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ErrorResponse {
    private String errorCode;
    private String message;
    private LocalDateTime timestamp;
    private Map details;

    public ErrorResponse(ErrorCode errorCode, String message) {
        this.errorCode = errorCode.getCode();
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.details = new HashMap<>();
    }

    public ErrorResponse(ErrorCode errorCode, String message, Map details) {
        this.errorCode = errorCode.getCode();
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.details = details;
    }

    // Getters and setters
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public Map getDetails() { return details; }
    public void setDetails(Map details) { this.details = details; }
}