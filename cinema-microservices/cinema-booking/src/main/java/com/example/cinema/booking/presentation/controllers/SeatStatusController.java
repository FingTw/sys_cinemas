package com.example.cinema.booking.presentation.controllers;

import com.example.cinema.booking.application.dto.SeatStatusDTO;
import com.example.cinema.booking.application.usecases.BookingUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/showtimes")
public class SeatStatusController {

    private final BookingUseCase bookingUseCase;

    public SeatStatusController(BookingUseCase bookingUseCase) {
        this.bookingUseCase = bookingUseCase;
    }

    @GetMapping("/{showtimeId}/seats")
    public ResponseEntity<List<SeatStatusDTO>> getSeatStatuses(@PathVariable String showtimeId) {
        return ResponseEntity.ok(bookingUseCase.getSeatStatusesByShowtime(showtimeId));
    }
}
