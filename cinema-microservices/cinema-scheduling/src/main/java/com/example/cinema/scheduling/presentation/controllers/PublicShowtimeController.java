package com.example.cinema.scheduling.presentation.controllers;

import com.example.cinema.scheduling.application.dto.ShowtimeDTO;
import com.example.cinema.scheduling.application.usecases.ShowtimeUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/showtimes")
public class PublicShowtimeController {

    private static final Logger log = LoggerFactory.getLogger(PublicShowtimeController.class);

    private final ShowtimeUseCase showtimeUseCase;

    public PublicShowtimeController(ShowtimeUseCase showtimeUseCase) {
        this.showtimeUseCase = showtimeUseCase;
    }

    @GetMapping
    public ResponseEntity<List<ShowtimeDTO>> getAllShowtimes() {
        log.info("[Public] Fetching all showtimes");
        return ResponseEntity.ok(showtimeUseCase.getAllShowtimes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShowtimeDTO> getShowtimeById(@PathVariable String id) {
        log.info("[Public] Fetching showtime ID: [{}]", id);
        return ResponseEntity.ok(showtimeUseCase.getShowtimeById(id));
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<ShowtimeDTO>> getShowtimesByMovie(@PathVariable String movieId) {
        log.info("[Public] Fetching showtimes for movie ID: [{}]", movieId);
        return ResponseEntity.ok(showtimeUseCase.getShowtimesByMovie(movieId));
    }

    // Endpoint getSeatStatuses da duoc chuyen sang Booking Service
}
