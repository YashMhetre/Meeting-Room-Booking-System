package com.meetingroom.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.meetingroom.model.MeetingRoom;

public interface MeetingRoomRepository extends JpaRepository<MeetingRoom, Long> {
    Optional<MeetingRoom> findByName(String name);
}