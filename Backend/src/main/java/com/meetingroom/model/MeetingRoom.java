package com.meetingroom.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

@Entity
@Table(name = "meeting_rooms")
public class MeetingRoom {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(nullable = false)
    private Integer capacity;
    
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    @JsonIgnore  // ← ADD THIS LINE
    private List<Booking> bookings;
    
    // Constructors
    public MeetingRoom() {}
    
    public MeetingRoom(String name, Integer capacity) {
        this.name = name;
        this.capacity = capacity;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    
    public List<Booking> getBookings() { return bookings; }
    public void setBookings(List<Booking> bookings) { this.bookings = bookings; }
}