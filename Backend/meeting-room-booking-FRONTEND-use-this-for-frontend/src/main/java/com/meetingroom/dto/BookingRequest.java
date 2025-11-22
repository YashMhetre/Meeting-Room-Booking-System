package com.meetingroom.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class BookingRequest {
    
    @NotNull(message = "Room ID is required")
    private Long roomId;
    
    @NotBlank(message = "Booking date is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Booking date must be in format YYYY-MM-DD")
    private String bookingDate; // Format: YYYY-MM-DD
    
    @NotBlank(message = "Start time is required")
    @Pattern(regexp = "([01]\\d|2[0-3]):[0-5]\\d", message = "Start time must be in format HH:mm")
    private String startTime;   // Format: HH:mm
    
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes; // 30, 45, or 60
}