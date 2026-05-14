package com.example.cinema.infrastructure.scheduling;

import com.example.cinema.domain.entities.Showtime;
import com.example.cinema.domain.repositories.ShowtimeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler tự động cập nhật trạng thái suất chiếu:
 *   SCHEDULED → PLAYING (khi startTime đã qua)
 *   PLAYING   → COMPLETED (khi endTime đã qua)
 */
@Component
public class ShowtimeStatusScheduler {

    private static final Logger log = LoggerFactory.getLogger(ShowtimeStatusScheduler.class);

    private final ShowtimeRepository showtimeRepository;

    public ShowtimeStatusScheduler(ShowtimeRepository showtimeRepository) {
        this.showtimeRepository = showtimeRepository;
    }

    /**
     * Chạy mỗi 60 giây để cập nhật trạng thái suất chiếu.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void updateShowtimeStatuses() {
        LocalDateTime now = LocalDateTime.now();

        // 1. SCHEDULED → PLAYING: Suất chiếu đã bắt đầu
        List<Showtime> startedShowtimes = showtimeRepository.findScheduledStartedBefore(now);
        if (!startedShowtimes.isEmpty()) {
            log.info("Dang cap nhat {} suat chieu tu SCHEDULED -> PLAYING...", startedShowtimes.size());
            for (Showtime showtime : startedShowtimes) {
                showtime.setStatus("PLAYING");
                showtimeRepository.save(showtime);
                log.info("Suat chieu [{}] da bat dau chieu (PLAYING)", showtime.getId());
            }
        }

        // 2. PLAYING → COMPLETED: Suất chiếu đã kết thúc
        List<Showtime> endedShowtimes = showtimeRepository.findPlayingEndedBefore(now);
        if (!endedShowtimes.isEmpty()) {
            log.info("Dang cap nhat {} suat chieu tu PLAYING -> COMPLETED...", endedShowtimes.size());
            for (Showtime showtime : endedShowtimes) {
                showtime.setStatus("COMPLETED");
                showtimeRepository.save(showtime);
                log.info("Suat chieu [{}] da ket thuc (COMPLETED)", showtime.getId());
            }
        }
    }
}
