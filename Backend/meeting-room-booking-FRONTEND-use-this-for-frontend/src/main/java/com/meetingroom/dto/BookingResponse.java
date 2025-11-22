package com.meetingroom.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long id;
    private Long roomId;
    private String roomName;
    private Long userId;
    private String userName;
    private String userEmail;
    private String bookingDate;
    private String startTime;
    private String endTime;
    private Integer durationMinutes;
    private String status;
    private String createdAt;
    private String completedAt;
}