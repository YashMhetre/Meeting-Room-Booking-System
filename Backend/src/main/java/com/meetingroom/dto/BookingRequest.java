package com.meetingroom.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;

public class BookingRequest {
    
    @NotNull(message = "Room ID is required")
    private Long roomId;
    
    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalDateTime endTime;
    
    // Remove bookedBy field - we'll get it from JWT token
    
    public BookingRequest() {}
    
    // Getters and Setters
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}