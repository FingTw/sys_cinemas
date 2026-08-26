package com.example.cinema.admin.repositories;

import com.example.cinema.admin.entities.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, String> {
    List<Seat> findByRoomId(String roomId);
    int countByRoomId(String roomId);
    void deleteByRoomId(String roomId);
}
