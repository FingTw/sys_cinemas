package com.example.cinema.scheduling.presentation.controllers;

import com.example.cinema.scheduling.application.dto.ShowtimeDTO;
import com.example.cinema.scheduling.application.ports.in.ShowtimeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/showtimes")
@RequiredArgsConstructor
@Slf4j
public class PublicShowtimeController {

    private final ShowtimeService showtimeService;

    @GetMapping
    public ResponseEntity<List<ShowtimeDTO>> getAllShowtimes() {
        log.info("[Public] Fetching all showtimes");
        return ResponseEntity.ok(showtimeService.getAllShowtimes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShowtimeDTO> getShowtimeById(@PathVariable String id) {
        log.info("[Public] Fetching showtime ID: [{}]", id);
        return ResponseEntity.ok(showtimeService.getShowtimeById(id));
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<ShowtimeDTO>> getShowtimesByMovie(@PathVariable String movieId) {
        log.info("[Public] Fetching showtimes for movie ID: [{}]", movieId);
        return ResponseEntity.ok(showtimeService.getShowtimesByMovie(movieId));
    }
}
