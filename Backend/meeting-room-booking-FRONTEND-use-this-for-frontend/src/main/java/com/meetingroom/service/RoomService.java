package com.meetingroom.service;

import com.meetingroom.exception.ErrorCode;
import com.meetingroom.exception.DuplicateResourceException;
import com.meetingroom.exception.ResourceNotFoundException;
import com.meetingroom.model.Room;
import com.meetingroom.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Cacheable(value = "rooms", key = "'all'")
    public List<Room> getAllRooms() {
        System.out.println("Fetching all rooms from database..."); // For testing
        return roomRepository.findAll();
    }

    @Cacheable(value = "rooms", key = "#id")
    public Room getRoomById(Long id) {
        System.out.println("Fetching room " + id + " from database..."); // For testing
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    ErrorCode.ROOM_NOT_FOUND,
                    "Room not found with id: " + id
                ));
    }

    @Caching(evict = {
        @CacheEvict(value = "rooms", key = "'all'"),
        @CacheEvict(value = "roomAvailability", allEntries = true)
    })
    public Room createRoom(String name, Integer capacity, Boolean available) {
        if (roomRepository.findByName(name).isPresent()) {
            throw new DuplicateResourceException(
                ErrorCode.ROOM_NAME_DUPLICATE,
                "Room with name '" + name + "' already exists"
            );
        }

        Room room = new Room();
        room.setName(name);
        room.setCapacity(capacity);
        room.setAvailable(available != null ? available : true);

        return roomRepository.save(room);
    }

    @Caching(evict = {
        @CacheEvict(value = "rooms", key = "#id"),
        @CacheEvict(value = "rooms", key = "'all'"),
        @CacheEvict(value = "roomAvailability", allEntries = true)
    })
    public Room updateRoom(Long id, String name, Integer capacity, Boolean available) {
        Room room = getRoomById(id);

        if (name != null) {
            roomRepository.findByName(name).ifPresent(existingRoom -> {
                if (!existingRoom.getId().equals(id)) {
                    throw new DuplicateResourceException(
                        ErrorCode.ROOM_NAME_DUPLICATE,
                        "Room with name '" + name + "' already exists"
                    );
                }
            });
            room.setName(name);
        }

        if (capacity != null) {
            room.setCapacity(capacity);
        }

        if (available != null) {
            room.setAvailable(available);
        }

        return roomRepository.save(room);
    }

    @Caching(evict = {
        @CacheEvict(value = "rooms", key = "#id"),
        @CacheEvict(value = "rooms", key = "'all'"),
        @CacheEvict(value = "roomAvailability", allEntries = true)
    })
    public boolean deleteRoom(Long id) {
        Room room = getRoomById(id);
        roomRepository.delete(room);
        return true;
    }

    @Cacheable(value = "rooms", key = "'available'")
    public List<Room> getAvailableRooms() {
        return roomRepository.findByAvailable(true);
    }
}