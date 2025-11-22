package com.meetingroom.service;

import com.meetingroom.dto.BookingRequest;
import com.meetingroom.dto.RoomAvailabilityResponse;
import com.meetingroom.model.Booking;
import com.meetingroom.model.Booking.BookingStatus;
import com.meetingroom.model.Room;
import com.meetingroom.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomService roomService;

    @Transactional
    @CacheEvict(value = {"roomAvailability", "userBookings"}, allEntries = true)
    public Booking createBookingWithKeycloak(BookingRequest request, String userEmail, String userName) {
        Room room = roomService.getRoomById(request.getRoomId());

        LocalDate bookingDate = LocalDate.parse(request.getBookingDate());
        LocalTime startTime = LocalTime.parse(request.getStartTime());
        LocalTime endTime = startTime.plusMinutes(request.getDurationMinutes());

        LocalTime businessStart = LocalTime.of(9, 0);
        LocalTime businessEnd = LocalTime.of(18, 0);

        if (startTime.isBefore(businessStart) || endTime.isAfter(businessEnd)) {
            throw new RuntimeException("Booking must be within business hours (9 AM - 6 PM)");
        }

        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                room.getId(), bookingDate, startTime, endTime
        );

        if (!overlappingBookings.isEmpty()) {
            throw new RuntimeException("Time slot already booked. Please choose another time.");
        }

        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setUserEmail(userEmail);
        booking.setUserName(userName);
        booking.setBookingDate(bookingDate);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setDurationMinutes(request.getDurationMinutes());
        booking.setStatus(BookingStatus.ACTIVE);

        return bookingRepository.save(booking);
    }

    // Cache user bookings - frequently accessed
    @Cacheable(value = "userBookings", key = "#userEmail")
    public List<Booking> getUserBookingsByEmail(String userEmail) {
        return bookingRepository.findByUserEmailOrderByBookingDateDescStartTimeDesc(userEmail);
    }

    @Cacheable(value = "userBookings", key = "#userEmail + '_' + #date")
    public List<Booking> getUserBookingsByDateAndEmail(String userEmail, String date) {
        LocalDate bookingDate = LocalDate.parse(date);
        return bookingRepository.findByUserEmailAndBookingDateOrderByStartTimeAsc(userEmail, bookingDate);
    }

    @Cacheable(value = "userBookings", key = "#userEmail + '_room_' + #roomId")
    public List<Booking> getUserBookingsByRoomAndEmail(String userEmail, Long roomId) {
        return bookingRepository.findByUserEmailAndRoomIdOrderByBookingDateDescStartTimeDesc(userEmail, roomId);
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
    }

    @Transactional
    @CacheEvict(value = {"roomAvailability", "userBookings"}, allEntries = true)
    public Booking completeBookingByEmail(Long id, String userEmail) {
        Booking booking = getBookingById(id);

        if (!booking.getUserEmail().equals(userEmail)) {
            throw new RuntimeException("You can only complete your own bookings");
        }

        if (booking.getStatus() != BookingStatus.ACTIVE) {
            throw new RuntimeException("Booking is not active");
        }

        booking.setStatus(BookingStatus.COMPLETED);
        booking.setCompletedAt(LocalDateTime.now());

        return bookingRepository.save(booking);
    }

    @Transactional
    @CacheEvict(value = {"roomAvailability", "userBookings"}, allEntries = true)
    public Booking cancelBookingByEmail(Long id, String userEmail) {
        Booking booking = getBookingById(id);

        if (!booking.getUserEmail().equals(userEmail)) {
            throw new RuntimeException("You can only cancel your own bookings");
        }

        if (booking.getStatus() != BookingStatus.ACTIVE) {
            throw new RuntimeException("Booking is not active");
        }

        booking.setStatus(BookingStatus.CANCELLED);

        return bookingRepository.save(booking);
    }

    // Cache room availability - most frequently accessed
    @Cacheable(value = "roomAvailability", key = "#roomId + '_' + #date")
    public RoomAvailabilityResponse getRoomAvailability(Long roomId, String date) {
        Room room = roomService.getRoomById(roomId);
        LocalDate bookingDate = LocalDate.parse(date);

        List<Booking> bookings = bookingRepository.findByRoomIdAndBookingDateAndStatusOrderByStartTimeAsc(
                roomId, bookingDate, BookingStatus.ACTIVE
        );

        List<RoomAvailabilityResponse.TimeSlot> bookedSlots = bookings.stream()
                .map(booking -> new RoomAvailabilityResponse.TimeSlot(
                        booking.getStartTime().toString(),
                        booking.getEndTime().toString(),
                        booking.getUserName(),
                        booking.getId()
                ))
                .collect(Collectors.toList());

        return new RoomAvailabilityResponse(
                room.getId(),
                room.getName(),
                room.getCapacity(),
                bookedSlots
        );
    }

    // Cache all room availabilities for a date
    @Cacheable(value = "roomAvailability", key = "'all_' + #date")
    public List<RoomAvailabilityResponse> getAllRoomAvailabilities(String date) {
        List<Room> rooms = roomService.getAllRooms();
        return rooms.stream()
                .map(room -> getRoomAvailability(room.getId(), date))
                .collect(Collectors.toList());
    }
}