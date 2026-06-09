package com.example.cinema.booking.presentation.controllers;

import com.example.cinema.booking.application.dto.SeatStatusDTO;
import com.example.cinema.booking.application.ports.in.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/showtimes")
@RequiredArgsConstructor
public class SeatStatusController {

    private final BookingService bookingService;

    @GetMapping("/{showtimeId}/seats")
    public ResponseEntity<List<SeatStatusDTO>> getSeatStatuses(@PathVariable String showtimeId) {
        return ResponseEntity.ok(bookingService.getSeatStatusesByShowtime(showtimeId));
    }
}
