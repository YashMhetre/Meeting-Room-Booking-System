package com.meetingroom.resolver;

import com.meetingroom.dto.BookingRequest;
import com.meetingroom.dto.RoomAvailabilityResponse;
import com.meetingroom.model.Booking;
import com.meetingroom.model.Room;
import com.meetingroom.service.BookingService;
import com.meetingroom.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
public class GraphQLResolver {

    @Autowired
    private RoomService roomService;

    @Autowired
    private BookingService bookingService;

    // ============ Room Queries (Public) ============

    @QueryMapping
    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    @QueryMapping
    public Room getRoom(@Argument Long id) {
        return roomService.getRoomById(id);
    }

    @QueryMapping
    public RoomAvailabilityResponse getRoomAvailability(@Argument Long roomId, @Argument String date) {
        return bookingService.getRoomAvailability(roomId, date);
    }

    @QueryMapping
    public List<RoomAvailabilityResponse> getAllRoomAvailabilities(@Argument String date) {
        return bookingService.getAllRoomAvailabilities(date);
    }

    // ============ Room Mutations (Admin only) ============

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Room createRoom(@Argument Map<String, Object> input) {
        String name = (String) input.get("name");
        Integer capacity = (Integer) input.get("capacity");
        Boolean available = input.containsKey("available") ? (Boolean) input.get("available") : true;
        return roomService.createRoom(name, capacity, available);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Room updateRoom(@Argument Long id, @Argument Map<String, Object> input) {
        String name = (String) input.get("name");
        Integer capacity = (Integer) input.get("capacity");
        Boolean available = (Boolean) input.get("available");
        return roomService.updateRoom(id, name, capacity, available);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deleteRoom(@Argument Long id) {
        return roomService.deleteRoom(id);
    }

    // ============ Booking Queries (Authenticated) ============

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<Booking> getMyBookings() {
        String email = getCurrentUserEmail();
        return bookingService.getUserBookingsByEmail(email);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<Booking> getMyBookingsByDate(@Argument String date) {
        String email = getCurrentUserEmail();
        return bookingService.getUserBookingsByDateAndEmail(email, date);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<Booking> getMyBookingsByRoom(@Argument Long roomId) {
        String email = getCurrentUserEmail();
        return bookingService.getUserBookingsByRoomAndEmail(email, roomId);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public Booking getBooking(@Argument Long id) {
        String currentUserEmail = getCurrentUserEmail();
        Booking booking = bookingService.getBookingById(id);
        
        // Check if user owns this booking (unless they're an admin)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin && !booking.getUserEmail().equals(currentUserEmail)) {
            throw new RuntimeException("You can only view your own bookings");
        }
        
        return booking;
    }

    // ============ Booking Mutations (Authenticated) ============

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Booking createBooking(@Argument Map<String, Object> input) {
        try {
            String email = getCurrentUserEmail();
            String name = getCurrentUserName();
            
            System.out.println("========================================");
            System.out.println("Creating booking with input: " + input);
            System.out.println("User email: " + email);
            System.out.println("User name: " + name);
            System.out.println("========================================");
            
            BookingRequest request = new BookingRequest();
            
            // Handle roomId - could be String or Integer from GraphQL
            Object roomIdObj = input.get("roomId");
            Long roomId;
            if (roomIdObj instanceof String) {
                roomId = Long.parseLong((String) roomIdObj);
            } else if (roomIdObj instanceof Integer) {
                roomId = ((Integer) roomIdObj).longValue();
            } else if (roomIdObj instanceof Long) {
                roomId = (Long) roomIdObj;
            } else {
                throw new RuntimeException("Invalid roomId type: " + roomIdObj.getClass());
            }
            
            request.setRoomId(roomId);
            request.setBookingDate((String) input.get("bookingDate"));
            request.setStartTime((String) input.get("startTime"));
            request.setDurationMinutes((Integer) input.get("durationMinutes"));
            
            System.out.println("Booking request: " + request);
            
            Booking booking = bookingService.createBookingWithKeycloak(request, email, name);
            System.out.println("✓ Booking created successfully with ID: " + booking.getId());
            System.out.println("========================================");
            
            return booking;
        } catch (Exception e) {
            System.err.println("========================================");
            System.err.println("✗ Error creating booking: " + e.getMessage());
            e.printStackTrace();
            System.err.println("========================================");
            throw new RuntimeException("Failed to create booking: " + e.getMessage(), e);
        }
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Booking completeBooking(@Argument Long id) {
        String email = getCurrentUserEmail();
        return bookingService.completeBookingByEmail(id, email);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Booking cancelBooking(@Argument Long id) {
        String email = getCurrentUserEmail();
        return bookingService.cancelBookingByEmail(id, email);
    }

    // ============ Helper Methods ============

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        
        // Extract email from JWT token
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String email = jwt.getClaim("email");
        System.out.println("Extracted email from JWT: " + email);
        return email;
    }
    
    private String getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        
        // Extract name from JWT token
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String name = jwt.getClaim("name");
        if (name == null) {
            name = jwt.getClaim("preferred_username");
        }
        String finalName = name != null ? name : "Unknown";
        System.out.println("Extracted name from JWT: " + finalName);
        return finalName;
    }
}