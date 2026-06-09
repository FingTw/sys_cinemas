package com.example.cinema.admin.domain.repositories;

import com.example.cinema.admin.domain.entities.Showtime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ShowtimeRepository {
    Showtime save(Showtime showtime);
    Optional<Showtime> findById(String id);
    List<Showtime> findAll();
    void deleteById(String id);
    List<Showtime> findConflicts(String roomId, LocalDateTime newStart, LocalDateTime newEnd);
    List<Showtime> findByMovieId(String movieId);
}
