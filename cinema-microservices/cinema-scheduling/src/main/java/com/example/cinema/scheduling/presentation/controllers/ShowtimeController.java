package com.example.cinema.scheduling.presentation.controllers;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.cinema.scheduling.application.dto.ShowtimeDTO;
import com.example.cinema.scheduling.application.dto.ShowtimeRequest;
import com.example.cinema.scheduling.application.usecases.ShowtimeUseCase;

@RestController
@RequestMapping("/api/v1/admin/showtimes")
public class ShowtimeController {

    private static final Logger log = LoggerFactory.getLogger(ShowtimeController.class);

    private final ShowtimeUseCase showtimeUseCase;

    public ShowtimeController(ShowtimeUseCase showtimeUseCase) {
        this.showtimeUseCase = showtimeUseCase;
    }

    @PreAuthorize("hasAuthority('SHOWTIME_CREATE')")
    @PostMapping
    public ResponseEntity<?> createShowtime(@RequestBody ShowtimeRequest request) {
        log.info("[POST /showtimes] - Request to create showtime: Movie=[{}], Room=[{}], StartTime=[{}]",
                request.getMovieId(), request.getRoomId(), request.getStartTime());
        try {
            ShowtimeDTO result = showtimeUseCase.createShowtime(request);
            log.info("Successfully created showtime: ID=[{}]", result.getId());
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            log.warn("Failed to create showtime: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasAuthority('SHOWTIME_READ')")
    @GetMapping
    public ResponseEntity<List<ShowtimeDTO>> getAllShowtimes() {
        log.info("[GET /showtimes] - Fetching all showtimes");
        return ResponseEntity.ok(showtimeUseCase.getAllShowtimes());
    }

    @PreAuthorize("hasAuthority('SHOWTIME_UPDATE')")
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelShowtime(@PathVariable String id) {
        log.info("[PUT /showtimes/{}/cancel] - Admin requested to cancel showtime", id);
        try {
            showtimeUseCase.cancelShowtime(id);
            log.info("Successfully cancelled showtime [{}]", id);
            return ResponseEntity.ok(Map.of("message", "Showtime cancelled successfully"));
        } catch (Exception e) {
            log.error("Failed to cancel showtime [{}]: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasAuthority('SHOWTIME_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteShowtime(@PathVariable String id) {
        log.info("[DELETE /showtimes/{}] - Admin requested to delete showtime", id);
        try {
            showtimeUseCase.deleteShowtime(id);
            log.info("Successfully deleted showtime [{}]", id);
            return ResponseEntity.ok(Map.of("message", "Showtime deleted successfully"));
        } catch (Exception e) {
            log.error("Failed to delete showtime [{}]: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
