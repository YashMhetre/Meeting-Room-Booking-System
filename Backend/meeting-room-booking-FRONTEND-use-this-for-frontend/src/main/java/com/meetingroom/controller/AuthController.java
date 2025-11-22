package com.meetingroom.controller;

import com.meetingroom.dto.TokenResponse;
import com.meetingroom.dto.LoginRequest;
import com.meetingroom.dto.RegisterRequest;
import com.meetingroom.service.UserRegistrationService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRegistrationService userRegistrationService;

    /**
     * Public endpoint for user registration
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
        	TokenResponse response = userRegistrationService.registerUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * Public endpoint for user login
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            TokenResponse response = userRegistrationService.loginUser(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Map.of("error", "Invalid email or password")
            );
        }
    }
}