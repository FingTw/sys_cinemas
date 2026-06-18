package com.example.cinema.admin.presentation.controllers;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.example.cinema.admin.application.ports.in.AdminShowtimeUseCase;
import com.example.cinema.admin.application.dto.ShowtimeDTO;
import com.example.cinema.admin.application.dto.ShowtimeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/admin/showtimes")
@RequiredArgsConstructor
@Slf4j
public class AdminShowtimeController {

    private final AdminShowtimeUseCase adminShowtimeUseCase;
    private final StringRedisTemplate redisTemplate;

    private void clearShowtimeCache() {
        try {
            // 1. Xóa cache Gateway aggregation BFF (nếu Suất chiếu đổi thì Home Page quick booking hoặc carousel có thể đổi)
            redisTemplate.delete("cache:home-overview");

            // 2. Xóa các cache Suất chiếu của Scheduling Service
            java.util.Set<String> showtimesKeys = redisTemplate.keys("showtimes::*");
            if (showtimesKeys != null && !showtimesKeys.isEmpty()) {
                redisTemplate.delete(showtimesKeys);
                log.info("[CINEMA-ADMIN] Evicted showtimes cache keys: {}", showtimesKeys);
            }

            java.util.Set<String> showtimeKeys = redisTemplate.keys("showtime::*");
            if (showtimeKeys != null && !showtimeKeys.isEmpty()) {
                redisTemplate.delete(showtimeKeys);
                log.info("[CINEMA-ADMIN] Evicted showtime detail cache keys: {}", showtimeKeys);
            }

            java.util.Set<String> byMovieKeys = redisTemplate.keys("showtimesByMovie::*");
            if (byMovieKeys != null && !byMovieKeys.isEmpty()) {
                redisTemplate.delete(byMovieKeys);
                log.info("[CINEMA-ADMIN] Evicted showtimesByMovie cache keys: {}", byMovieKeys);
            }
        } catch (Exception e) {
            log.warn("[CINEMA-ADMIN] Failed to clear showtime caches: {}", e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('SHOWTIME_CREATE')")
    @PostMapping
    public ResponseEntity<?> createShowtime(@RequestBody ShowtimeRequest request) {
        log.info("Admin request to create showtime: Movie=[{}], Room=[{}], StartTime=[{}]",
                request.getMovieId(), request.getRoomId(), request.getStartTime());
        try {
            ShowtimeDTO result = adminShowtimeUseCase.createShowtime(request);
            clearShowtimeCache();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.warn("Failed to create showtime: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasAuthority('SHOWTIME_READ')")
    @GetMapping
    public ResponseEntity<List<ShowtimeDTO>> getAllShowtimes() {
        log.info("Fetching all showtimes for admin");
        return ResponseEntity.ok(adminShowtimeUseCase.getAllShowtimes());
    }

    @PreAuthorize("hasAuthority('SHOWTIME_UPDATE')")
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelShowtime(@PathVariable String id) {
        log.info("Admin request to cancel showtime ID: [{}]", id);
        try {
            adminShowtimeUseCase.cancelShowtime(id);
            clearShowtimeCache();
            return ResponseEntity.ok(Map.of("message", "Showtime cancelled successfully"));
        } catch (Exception e) {
            log.error("Failed to cancel showtime [{}]: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasAuthority('SHOWTIME_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteShowtime(@PathVariable String id) {
        log.info("Admin request to delete showtime ID: [{}]", id);
        try {
            adminShowtimeUseCase.deleteShowtime(id);
            clearShowtimeCache();
            return ResponseEntity.ok(Map.of("message", "Showtime deleted successfully"));
        } catch (Exception e) {
            log.error("Failed to delete showtime [{}]: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
