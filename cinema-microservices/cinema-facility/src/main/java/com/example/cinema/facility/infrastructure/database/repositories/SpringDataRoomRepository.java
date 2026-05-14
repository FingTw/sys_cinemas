package com.example.cinema.facility.infrastructure.database.repositories;

import com.example.cinema.facility.infrastructure.database.entities.RoomJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataRoomRepository extends JpaRepository<RoomJpaEntity, String> {
}
