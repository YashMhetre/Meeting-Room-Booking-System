package com.meetingroom.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

import com.meetingroom.model.BookingStatus;

@Data
@AllArgsConstructor
public class BookingResponse {
    private Long id;
    private Long roomId;
    private String roomName;
    private String bookedBy;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BookingStatus status;
}