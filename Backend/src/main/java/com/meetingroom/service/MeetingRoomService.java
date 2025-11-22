package com.meetingroom.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.meetingroom.exception.ResourceNotFoundException;
import com.meetingroom.model.MeetingRoom;
import com.meetingroom.repository.MeetingRoomRepository;



@Service
public class MeetingRoomService {
    
    @Autowired
    private MeetingRoomRepository roomRepository;
    
    public List<MeetingRoom> getAllRooms() {
        return roomRepository.findAll();
    }
    
    public MeetingRoom getRoomById(Long id) {
        return roomRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
    }
}