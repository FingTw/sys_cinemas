package com.example.cinema.infrastructure.database.repositories;

import com.example.cinema.infrastructure.database.entities.ShowtimeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SpringDataShowtimeRepository extends JpaRepository<ShowtimeJpaEntity, String> {

    /**
     * Truy vấn SQL phát hiện xung đột suất chiếu:
     * Hai khoảng thời gian [A_start, A_end] và [B_start, B_end] chồng nhau
     * khi và chỉ khi: A_start < B_end AND A_end > B_start
     * Chỉ xét các suất chiếu chưa bị hủy.
     */
    @Query("SELECT s FROM ShowtimeJpaEntity s WHERE s.roomId = :roomId " +
           "AND s.startTime < :newEnd AND s.endTime > :newStart " +
           "AND s.status <> 'CANCELLED'")
    List<ShowtimeJpaEntity> findConflicts(
            @Param("roomId") String roomId,
            @Param("newStart") LocalDateTime newStart,
            @Param("newEnd") LocalDateTime newEnd
    );

    List<ShowtimeJpaEntity> findByMovieId(String movieId);

    // Scheduler: Suất chiếu SCHEDULED đã bắt đầu
    List<ShowtimeJpaEntity> findByStatusAndStartTimeLessThanEqual(String status, LocalDateTime now);

    // Scheduler: Suất chiếu PLAYING đã kết thúc
    List<ShowtimeJpaEntity> findByStatusAndEndTimeLessThanEqual(String status, LocalDateTime now);
}
