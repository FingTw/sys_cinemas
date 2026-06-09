package com.example.cinema.facility.infrastructure.database.repositories;

import com.example.cinema.facility.infrastructure.database.entities.SeatJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SpringDataSeatRepository extends JpaRepository<SeatJpaEntity, String> {
    List<SeatJpaEntity> findByRoomId(String roomId);
    int countByRoomId(String roomId);
    void deleteByRoomId(String roomId);
}
