package com.example.cinema.booking.presentation.controllers;

import com.example.cinema.booking.application.dto.BookingDetailResponse;
import com.example.cinema.booking.application.ports.in.BookingQueryUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller quản lý Booking dành cho Admin/Staff.
 * Endpoint: /api/v1/admin/bookings
 */
@RestController
@RequestMapping("/api/v1/admin/bookings")
public class AdminBookingController {

    private final BookingQueryUseCase bookingQueryUseCase;
    private final com.example.cinema.booking.application.usecases.BookingUseCase bookingUseCase;

    public AdminBookingController(BookingQueryUseCase bookingQueryUseCase, com.example.cinema.booking.application.usecases.BookingUseCase bookingUseCase) {
        this.bookingQueryUseCase = bookingQueryUseCase;
        this.bookingUseCase = bookingUseCase;
    }

    /**
     * Xem tất cả đơn đặt vé (bao gồm thông tin phim, phòng, ghế, user).
     */
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    @GetMapping
    public ResponseEntity<java.util.List<BookingDetailResponse>> getAllBookings() {
        return ResponseEntity.ok(bookingQueryUseCase.getAllBookings());
    }

    /**
     * Bán vé trực tiếp tại quầy.
     */
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    @PostMapping("/direct")
    public ResponseEntity<com.example.cinema.booking.application.dto.BookingResponse> createDirectBooking(
            @RequestBody com.example.cinema.booking.application.dto.CreateBookingRequest request,
            org.springframework.security.core.Authentication authentication) {
        String staffId = authentication.getName();
        return ResponseEntity.ok(bookingUseCase.createDirectBooking(request, staffId));
    }
}
