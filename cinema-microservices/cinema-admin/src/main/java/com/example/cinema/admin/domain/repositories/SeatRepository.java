package com.example.cinema.admin.domain.repositories;

import com.example.cinema.admin.domain.entities.Seat;
import java.util.List;
import java.util.Optional;

public interface SeatRepository {
    Seat save(Seat seat);
    List<Seat> saveAll(List<Seat> seats);
    Optional<Seat> findById(String id);
    List<Seat> findByRoomId(String roomId);
    int countByRoomId(String roomId);
    void deleteByRoomId(String roomId);
}
