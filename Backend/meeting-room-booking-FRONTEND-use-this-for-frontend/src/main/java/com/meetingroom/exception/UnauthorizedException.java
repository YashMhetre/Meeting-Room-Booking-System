package com.meetingroom.exception;

public class UnauthorizedException extends RuntimeException {
    private final ErrorCode errorCode;

    public UnauthorizedException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
