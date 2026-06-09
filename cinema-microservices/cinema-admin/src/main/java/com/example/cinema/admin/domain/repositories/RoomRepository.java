package com.example.cinema.admin.domain.repositories;

import com.example.cinema.admin.domain.entities.Room;
import java.util.List;
import java.util.Optional;

public interface RoomRepository {
    Room save(Room room);
    Optional<Room> findById(String id);
    List<Room> findAll();
    void deleteById(String id);
}
