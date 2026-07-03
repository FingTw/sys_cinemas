package com.example.cinema.scheduling.application.scheduler;

import com.example.cinema.scheduling.domain.entities.Showtime;
import com.example.cinema.scheduling.domain.repositories.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShowtimeStatusScheduler {

    private final ShowtimeRepository showtimeRepository;
    private final CacheManager cacheManager;

    /**
     * Định kỳ quét mỗi 1 phút để cập nhật trạng thái suất chiếu quá hạn
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkAndUpdateShowtimeStatuses() {
        LocalDateTime now = LocalDateTime.now();
        boolean hasChanges = false;

        // 1. Cập nhật SCHEDULED -> PLAYING (Các suất chiếu đã bắt đầu)
        List<Showtime> scheduledStarted = showtimeRepository.findScheduledStartedBefore(now);
        if (!scheduledStarted.isEmpty()) {
            log.info("[Scheduler] Phát hiện {} suất chiếu đã đến giờ bắt đầu. Đang cập nhật sang PLAYING...", scheduledStarted.size());
            for (Showtime st : scheduledStarted) {
                Showtime updated = Showtime.builder()
                        .id(st.getId())
                        .movieId(st.getMovieId())
                        .roomId(st.getRoomId())
                        .startTime(st.getStartTime())
                        .endTime(st.getEndTime())
                        .status("PLAYING")
                        .price(st.getPrice())
                        .priceVip(st.getPriceVip())
                        .priceCouple(st.getPriceCouple())
                        .build();
                showtimeRepository.save(updated);
                log.info("[Scheduler] Đã chuyển trạng thái suất chiếu [{}] sang PLAYING (startTime: {})", st.getId(), st.getStartTime());
            }
            hasChanges = true;
        }

        // 2. Cập nhật PLAYING -> COMPLETED (Các suất chiếu đã kết thúc)
        List<Showtime> playingEnded = showtimeRepository.findPlayingEndedBefore(now);
        if (!playingEnded.isEmpty()) {
            log.info("[Scheduler] Phát hiện {} suất chiếu đã đến giờ kết thúc. Đang cập nhật sang COMPLETED...", playingEnded.size());
            for (Showtime st : playingEnded) {
                Showtime updated = Showtime.builder()
                        .id(st.getId())
                        .movieId(st.getMovieId())
                        .roomId(st.getRoomId())
                        .startTime(st.getStartTime())
                        .endTime(st.getEndTime())
                        .status("COMPLETED")
                        .price(st.getPrice())
                        .priceVip(st.getPriceVip())
                        .priceCouple(st.getPriceCouple())
                        .build();
                showtimeRepository.save(updated);
                log.info("[Scheduler] Đã chuyển trạng thái suất chiếu [{}] sang COMPLETED (endTime: {})", st.getId(), st.getEndTime());
            }
            hasChanges = true;
        }

        // Xóa cache nếu có bất kỳ thay đổi trạng thái nào
        if (hasChanges) {
            clearShowtimeCaches();
        }
    }

    private void clearShowtimeCaches() {
        log.info("[Scheduler] Có sự thay đổi trạng thái suất chiếu. Tiến hành xóa cache...");
        try {
            Cache showtimesCache = cacheManager.getCache("showtimes");
            if (showtimesCache != null) {
                showtimesCache.clear();
                log.info("[Scheduler] Đã xóa cache 'showtimes'");
            }
            
            Cache showtimeCache = cacheManager.getCache("showtime");
            if (showtimeCache != null) {
                showtimeCache.clear();
                log.info("[Scheduler] Đã xóa cache 'showtime'");
            }
            
            Cache showtimesByMovieCache = cacheManager.getCache("showtimesByMovie");
            if (showtimesByMovieCache != null) {
                showtimesByMovieCache.clear();
                log.info("[Scheduler] Đã xóa cache 'showtimesByMovie'");
            }
        } catch (Exception e) {
            log.error("[Scheduler] Gặp lỗi khi xóa cache suất chiếu: {}", e.getMessage(), e);
        }
    }
}
