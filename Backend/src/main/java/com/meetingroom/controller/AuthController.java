package com.meetingroom.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.meetingroom.config.JwtUtil;
import com.meetingroom.dto.LoginRequest;
import com.meetingroom.dto.LoginResponse;
import com.meetingroom.dto.RegisterRequest;
import com.meetingroom.service.UserService;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired 
    private UserService userService;
    
    @Autowired 
    private JwtUtil jwtUtil;
    
    @Autowired 
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        if (userService.emailExists(req.getEmail())) {
            return ResponseEntity.badRequest().body("Email already registered");
        }

        userService.registerUser(req);
        return ResponseEntity.ok("Registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        return userService.findByEmail(req.getEmail())
            .map(user -> {
                if (passwordEncoder.matches(req.getPassword(), user.getPassword())) {
                    String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
                    return ResponseEntity.ok(new LoginResponse(token, user.getRole(), user.getName()));
                } else {
                    return ResponseEntity.status(401).body("Invalid credentials");
                }
            })
            .orElseGet(() -> ResponseEntity.status(401).body("Invalid credentials"));
    }
}
