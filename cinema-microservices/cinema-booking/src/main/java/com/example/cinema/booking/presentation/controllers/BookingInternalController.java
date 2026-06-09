package com.example.cinema.booking.presentation.controllers;

import com.example.cinema.booking.application.dto.BookingDetailResponse;
import com.example.cinema.booking.application.ports.in.BookingQueryUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Internal API — dành cho inter-service communication.
 * Được gọi bởi cinema-admin để lấy booking detail cho JasperReport PDF generation.
 * Không kiểm tra quyền sở hữu (userId), chỉ kiểm tra ROLE_ADMIN.
 */
@RestController
@RequestMapping("/api/internal/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingInternalController {

    private final BookingQueryUseCase bookingQueryUseCase;

    /**
     * Lấy chi tiết booking theo ID — không kiểm tra userId ownership.
     * Dùng cho admin: in vé PDF, in receipt PDF từ cinema-admin service.
     */
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDetailResponse> getBookingDetailInternal(
            @PathVariable String bookingId) {
        log.info("[INTERNAL] Fetching booking detail for ID: [{}]", bookingId);
        BookingDetailResponse detail = bookingQueryUseCase.getBookingDetailInternal(bookingId);
        return ResponseEntity.ok(detail);
    }
}
