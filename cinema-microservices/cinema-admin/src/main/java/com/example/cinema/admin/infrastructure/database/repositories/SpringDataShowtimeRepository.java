package com.example.cinema.admin.infrastructure.database.repositories;

import com.example.cinema.admin.infrastructure.database.entities.ShowtimeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SpringDataShowtimeRepository extends JpaRepository<ShowtimeJpaEntity, String> {

    @Query("SELECT s FROM ShowtimeJpaEntity s WHERE s.roomId = :roomId " +
           "AND s.startTime < :newEnd AND s.endTime > :newStart " +
           "AND s.status <> 'CANCELLED'")
    List<ShowtimeJpaEntity> findConflicts(
            @Param("roomId") String roomId,
            @Param("newStart") LocalDateTime newStart,
            @Param("newEnd") LocalDateTime newEnd
    );

    List<ShowtimeJpaEntity> findByMovieId(String movieId);
}
