package com.meetingroom.service;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.meetingroom.dto.RegisterRequest;
import com.meetingroom.model.User;
import com.meetingroom.repository.UserRepository;

import java.util.Optional;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ModelMapper modelMapper;

    public User registerUser(RegisterRequest req) {
        // Map RegisterRequest to User
        User user = modelMapper.map(req, User.class);

        // Encode password and set role manually
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole("USER");

        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}