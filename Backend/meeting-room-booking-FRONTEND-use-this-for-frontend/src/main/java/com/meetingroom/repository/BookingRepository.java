// BookingRepository.java
package com.meetingroom.repository;

import com.meetingroom.model.Booking;
import com.meetingroom.model.Booking.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    // Find all bookings for a specific user email
    List<Booking> findByUserEmailOrderByBookingDateDescStartTimeDesc(String userEmail);
    
    // Find bookings by user email and date
    List<Booking> findByUserEmailAndBookingDateOrderByStartTimeAsc(String userEmail, LocalDate date);
    
    // Find bookings by user email and room
    List<Booking> findByUserEmailAndRoomIdOrderByBookingDateDescStartTimeDesc(String userEmail, Long roomId);
    
    // Find bookings by room and date
    List<Booking> findByRoomIdAndBookingDateOrderByStartTimeAsc(Long roomId, LocalDate date);
    
    // Find all bookings for a specific date
    List<Booking> findByBookingDateOrderByRoomIdAscStartTimeAsc(LocalDate date);
    
    // Check for overlapping bookings
    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId " +
           "AND b.bookingDate = :date " +
           "AND b.status = 'ACTIVE' " +
           "AND ((b.startTime < :endTime AND b.endTime > :startTime))")
    List<Booking> findOverlappingBookings(
        @Param("roomId") Long roomId,
        @Param("date") LocalDate date,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime
    );
    
    // Find active bookings by room and date
    List<Booking> findByRoomIdAndBookingDateAndStatusOrderByStartTimeAsc(
        Long roomId, LocalDate date, BookingStatus status
    );
}