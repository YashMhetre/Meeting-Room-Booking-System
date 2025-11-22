package com.meetingroom.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.meetingroom.dto.BookingRequest;
import com.meetingroom.dto.BookingResponse;
import com.meetingroom.service.BookingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "http://localhost:3000")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    /**
     * Create a new booking (authenticated user)
     */
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            Principal principal) {
        String userEmail = principal.getName();
        BookingResponse response = bookingService.createBooking(request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get MY bookings with optional filters
     * 
     * Examples:
     * - /api/bookings/my-bookings → All my bookings
     * - /api/bookings/my-bookings?date=2025-11-17 → My bookings on Nov 17
     * - /api/bookings/my-bookings?roomId=1 → My bookings in Room 1
     * - /api/bookings/my-bookings?date=2025-11-17&roomId=1 → My bookings in Room 1 on Nov 17
     */
    @GetMapping("/my-bookings")
    public ResponseEntity<List<BookingResponse>> getMyBookings(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long roomId,
            Principal principal) {
        
        String userEmail = principal.getName();
        List<BookingResponse> bookings;
        
        if (date != null && roomId != null) {
            // Filter by BOTH date AND room
            bookings = bookingService.getMyBookingsByDateAndRoom(userEmail, date, roomId);
        } else if (date != null) {
            // Filter by date only
            bookings = bookingService.getMyBookingsByDate(userEmail, date);
        } else if (roomId != null) {
            // Filter by room only
            bookings = bookingService.getMyBookingsByRoom(userEmail, roomId);
        } else {
            // No filters - get all my bookings
            bookings = bookingService.getAllMyBookings(userEmail);
        }
        
        return ResponseEntity.ok(bookings);
    }

    /**
     * Complete my booking
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<Void> completeBooking(
            @PathVariable Long id,
            Principal principal) {
        String userEmail = principal.getName();
        bookingService.completeBooking(id, userEmail);
        return ResponseEntity.ok().build();
    }

    /**
     * Cancel my booking
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable Long id,
            Principal principal) {
        String userEmail = principal.getName();
        bookingService.cancelBooking(id, userEmail);
        return ResponseEntity.noContent().build();
    }
}