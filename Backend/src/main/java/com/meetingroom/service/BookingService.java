package com.meetingroom.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.meetingroom.dto.BookingRequest;
import com.meetingroom.dto.BookingResponse;
import com.meetingroom.model.User;
import com.meetingroom.exception.*;
import com.meetingroom.model.Booking;
import com.meetingroom.model.BookingStatus;
import com.meetingroom.model.MeetingRoom;
import com.meetingroom.repository.BookingRepository;
import com.meetingroom.repository.MeetingRoomRepository;
import com.meetingroom.repository.UserRepository;

@Service
@Transactional
public class BookingService {
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private MeetingRoomRepository roomRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public BookingResponse createBooking(BookingRequest request, String userEmail) {
        // Get authenticated user
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Validate room exists
        MeetingRoom room = roomRepository.findById(request.getRoomId())
            .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        
        // Validate time slot
        validateTimeSlot(request.getStartTime(), request.getEndTime());
        
        // Check for conflicts
        LocalDate bookingDate = request.getStartTime().toLocalDate();
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
            request.getRoomId(),
            bookingDate,
            request.getStartTime(),
            request.getEndTime(),
            BookingStatus.ACTIVE
        );
        
        if (!conflicts.isEmpty()) {
            throw new BookingConflictException(
                "Room is already booked for the selected time slot"
            );
        }
        
        // Create booking
        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setUser(user);
        booking.setBookedBy(user.getName());
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setBookingDate(bookingDate);
        booking.setStatus(BookingStatus.ACTIVE);
        
        Booking savedBooking = bookingRepository.save(booking);
        
        return mapToResponse(savedBooking);
    }
    
    /**
     * Get ALL my bookings (no filters)
     */
    public List<BookingResponse> getAllMyBookings(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return bookingRepository.findByUserAndStatus(user, BookingStatus.ACTIVE)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get my bookings filtered by DATE only
     */
    public List<BookingResponse> getMyBookingsByDate(String userEmail, LocalDate date) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return bookingRepository.findByUserAndBookingDateAndStatus(user, date, BookingStatus.ACTIVE)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get my bookings filtered by ROOM only
     */
    public List<BookingResponse> getMyBookingsByRoom(String userEmail, Long roomId) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        MeetingRoom room = roomRepository.findById(roomId)
            .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        
        return bookingRepository.findByUserAndRoomAndStatus(user, room, BookingStatus.ACTIVE)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get my bookings filtered by BOTH date AND room
     */
    public List<BookingResponse> getMyBookingsByDateAndRoom(String userEmail, LocalDate date, Long roomId) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        MeetingRoom room = roomRepository.findById(roomId)
            .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        
        return bookingRepository.findByUserAndBookingDateAndRoomAndStatus(
                user, date, room, BookingStatus.ACTIVE)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    public void completeBooking(Long bookingId, String userEmail) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        
        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new UnauthorizedException("You can only complete your own bookings");
        }
        
        booking.setStatus(BookingStatus.COMPLETED);
        bookingRepository.save(booking);
    }
    
    public void cancelBooking(Long bookingId, String userEmail) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        
        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new UnauthorizedException("You can only cancel your own bookings");
        }
        
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }
    
    private void validateTimeSlot(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end) || start.isEqual(end)) {
            throw new InvalidTimeSlotException("Invalid time slot");
        }
        
        if (start.isBefore(LocalDateTime.now())) {
            throw new InvalidTimeSlotException("Cannot book past time slots");
        }
    }
    
    private BookingResponse mapToResponse(Booking booking) {
        return new BookingResponse(
            booking.getId(),
            booking.getRoom().getId(),
            booking.getRoom().getName(),
            booking.getBookedBy(),
            booking.getStartTime(),
            booking.getEndTime(),
            booking.getStatus()
        );
    }
}