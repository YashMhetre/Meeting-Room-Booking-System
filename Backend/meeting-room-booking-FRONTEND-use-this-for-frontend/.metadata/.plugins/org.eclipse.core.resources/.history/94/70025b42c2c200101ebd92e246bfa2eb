package com.meetingroom.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

@Entity
@Table(name = "bookings")
public class Booking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private MeetingRoom room;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"password", "bookings"})
    private User user;

    
    @Column(nullable = false)
    private String bookedBy;  // Keep for display purposes
    
    @Column(nullable = false)
    private LocalDateTime startTime;
    
    @Column(nullable = false)
    private LocalDateTime endTime;
    
    @Column(nullable = false)
    private LocalDate bookingDate;
    
    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.ACTIVE;
    
    // Constructors
    public Booking() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public MeetingRoom getRoom() { return room; }
    public void setRoom(MeetingRoom room) { this.room = room; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public String getBookedBy() { return bookedBy; }
    public void setBookedBy(String bookedBy) { this.bookedBy = bookedBy; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }
    
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
}