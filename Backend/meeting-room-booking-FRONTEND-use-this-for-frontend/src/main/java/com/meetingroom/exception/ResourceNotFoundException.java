package com.meetingroom.exception;

public class ResourceNotFoundException extends RuntimeException {
    private final ErrorCode errorCode;

    public ResourceNotFoundException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ResourceNotFoundException(String message) {
        super(message);
        this.errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}