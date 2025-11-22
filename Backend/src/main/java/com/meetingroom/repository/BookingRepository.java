package com.meetingroom.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.meetingroom.model.Booking;
import com.meetingroom.model.BookingStatus;
import com.meetingroom.model.MeetingRoom;
import com.meetingroom.model.User;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Original methods
    List<Booking> findByBookingDateAndStatus(LocalDate date, BookingStatus status);

    List<Booking> findByRoomIdAndBookingDateAndStatus(
        Long roomId, LocalDate date, BookingStatus status
    );
    
    // User-specific queries
    
    /**
     * Get all bookings for a user (no filters)
     */
    List<Booking> findByUserAndStatus(User user, BookingStatus status);
    
    /**
     * Get user's bookings filtered by DATE
     */
    List<Booking> findByUserAndBookingDateAndStatus(
        User user, LocalDate date, BookingStatus status
    );
    
    /**
     * Get user's bookings filtered by ROOM
     */
    List<Booking> findByUserAndRoomAndStatus(
        User user, MeetingRoom room, BookingStatus status
    );
    
    /**
     * Get user's bookings filtered by BOTH date AND room
     */
    List<Booking> findByUserAndBookingDateAndRoomAndStatus(
        User user, LocalDate date, MeetingRoom room, BookingStatus status
    );

    // Conflict detection query
    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId " +
           "AND b.bookingDate = :date AND b.status = :status " +
           "AND ((b.startTime < :endTime AND b.endTime > :startTime))")
    List<Booking> findConflictingBookings(
        @Param("roomId") Long roomId,
        @Param("date") LocalDate date,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("status") BookingStatus status
    );
}