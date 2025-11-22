package com.meetingroom.exception;

public enum ErrorCode {
    // Authentication & Authorization
    USER_NOT_FOUND("AUTH_001", "User not found"),
    INVALID_CREDENTIALS("AUTH_002", "Invalid email or password"),
    EMAIL_ALREADY_EXISTS("AUTH_003", "Email already registered"),
    UNAUTHORIZED("AUTH_004", "Unauthorized access"),
    
    // Room errors
    ROOM_NOT_FOUND("ROOM_001", "Room not found"),
    ROOM_NAME_DUPLICATE("ROOM_002", "Room name already exists"),
    
    // Booking errors
    BOOKING_NOT_FOUND("BOOK_001", "Booking not found"),
    TIME_SLOT_UNAVAILABLE("BOOK_002", "Time slot already booked"),
    INVALID_BUSINESS_HOURS("BOOK_003", "Booking outside business hours"),
    BOOKING_NOT_ACTIVE("BOOK_004", "Booking is not active"),
    UNAUTHORIZED_BOOKING_ACCESS("BOOK_005", "Cannot modify another user's booking"),
    PAST_DATE_TIME_BOOKING("BOOK_006", "Cannot book for past date or time"),
    
    // Validation errors
    INVALID_INPUT("VAL_001", "Invalid input data"),
    INVALID_DATE_FORMAT("VAL_002", "Invalid date format"),
    INVALID_TIME_FORMAT("VAL_003", "Invalid time format"),
    
    // Generic errors
    INTERNAL_SERVER_ERROR("SYS_001", "Internal server error"),
    BAD_REQUEST("SYS_002", "Bad request");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}