package com.meetingroom.exception;

public class DuplicateResourceException extends RuntimeException {
    private final ErrorCode errorCode;

    public DuplicateResourceException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}