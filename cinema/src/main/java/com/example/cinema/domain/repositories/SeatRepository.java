package com.example.cinema.domain.repositories;

import com.example.cinema.domain.entities.Seat;
import java.util.List;
import java.util.Optional;

public interface SeatRepository {
    List<Seat> saveAll(List<Seat> seats);
    Seat save(Seat seat);
    Optional<Seat> findById(String id);
    List<Seat> findByRoomId(String roomId);
    void deleteByRoomId(String roomId);
    List<Seat> findAll();
}
