package com.example.cinema.admin.repositories;

import com.example.cinema.admin.entities.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, String> {

    @Query("SELECT s FROM Showtime s WHERE s.roomId = :roomId " +
           "AND s.startTime < :newEnd AND s.endTime > :newStart " +
           "AND s.status <> 'CANCELLED'")
    List<Showtime> findConflicts(
            @Param("roomId") String roomId,
            @Param("newStart") LocalDateTime newStart,
            @Param("newEnd") LocalDateTime newEnd
    );

    List<Showtime> findByMovieId(String movieId);
}
