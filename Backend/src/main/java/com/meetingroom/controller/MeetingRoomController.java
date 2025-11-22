package com.meetingroom.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.meetingroom.model.MeetingRoom;
import com.meetingroom.service.MeetingRoomService;


@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "http://localhost:3000")
public class MeetingRoomController {
    
    @Autowired
    private MeetingRoomService roomService;
    
    @GetMapping
    public ResponseEntity<List<MeetingRoom>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<MeetingRoom> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }
}