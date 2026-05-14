package com.example.cinema.scheduling.domain.repositories;

import com.example.cinema.scheduling.domain.entities.Showtime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ShowtimeRepository {
    Showtime save(Showtime showtime);
    Optional<Showtime> findById(String id);
    List<Showtime> findAll();
    void deleteById(String id);
    
    /**
     * Kiểm tra xung đột: Tìm tất cả suất chiếu trong cùng phòng
     * mà khoảng thời gian bị chồng lấn với [newStart, newEnd].
     * Logic: existing.startTime < newEnd AND existing.endTime > newStart
     */
    List<Showtime> findConflicts(String roomId, LocalDateTime newStart, LocalDateTime newEnd);

    List<Showtime> findByMovieId(String movieId);

    // Scheduler: Tìm suất chiếu SCHEDULED đã bắt đầu (startTime <= now)
    List<Showtime> findScheduledStartedBefore(LocalDateTime now);

    // Scheduler: Tìm suất chiếu PLAYING đã kết thúc (endTime <= now)
    List<Showtime> findPlayingEndedBefore(LocalDateTime now);
}
