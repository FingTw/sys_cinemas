package com.example.cinema.presentation.controllers;

import com.example.cinema.application.dto.SeatStatusDTO;
import com.example.cinema.application.dto.ShowtimeDTO;
import com.example.cinema.application.usecases.BookingUseCase;
import com.example.cinema.application.usecases.ShowtimeUseCase;
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

    private final BookingUseCase bookingUseCase;
    private final ShowtimeUseCase showtimeUseCase;

    public PublicShowtimeController(BookingUseCase bookingUseCase, ShowtimeUseCase showtimeUseCase) {
        this.bookingUseCase = bookingUseCase;
        this.showtimeUseCase = showtimeUseCase;
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<ShowtimeDTO>> getShowtimesByMovie(@PathVariable String movieId) {
        log.info("[Public] Fetching showtimes for movie ID: [{}]", movieId);
        return ResponseEntity.ok(showtimeUseCase.getShowtimesByMovie(movieId));
    }

    @GetMapping("/{id}/seats")
    public ResponseEntity<List<SeatStatusDTO>> getSeatStatuses(@PathVariable String id) {
        log.info("[Public] Fetching seat map for showtime ID: [{}]", id);
        return ResponseEntity.ok(bookingUseCase.getSeatStatusesByShowtime(id));
    }
}
