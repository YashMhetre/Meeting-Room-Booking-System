package com.meetingroom.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomAvailabilityResponse {
    private Long roomId;
    private String roomName;
    private Integer capacity;
    private List<TimeSlot> bookedSlots;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlot {
        private String startTime;
        private String endTime;
        private String bookedBy;
        private Long bookingId;
    }
}