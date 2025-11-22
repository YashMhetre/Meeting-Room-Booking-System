package com.meetingroom.exception;

import java.util.HashMap;
import java.util.Map;

public class BusinessRuleException extends RuntimeException {
    private final ErrorCode errorCode;
    private Map details = new HashMap<>();

    public BusinessRuleException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessRuleException(ErrorCode errorCode, String message, Map details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Map getDetails() {
        return details;
    }
}