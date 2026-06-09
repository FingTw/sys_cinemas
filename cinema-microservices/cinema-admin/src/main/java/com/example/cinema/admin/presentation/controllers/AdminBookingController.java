package com.example.cinema.admin.presentation.controllers;

import com.example.cinema.admin.application.dto.BookingDetailResponse;
import com.example.cinema.admin.application.dto.BookingResponse;
import com.example.cinema.admin.application.dto.CreateBookingRequest;
import com.example.cinema.admin.application.ports.in.AdminBookingUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/admin/bookings")
@RequiredArgsConstructor
@Slf4j
public class AdminBookingController {

    private final AdminBookingUseCase adminBookingUseCase;

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    @GetMapping
    public ResponseEntity<List<BookingDetailResponse>> getAllBookings() {
        log.info("Admin fetching all bookings");
        return ResponseEntity.ok(adminBookingUseCase.getAllBookings());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    @PostMapping("/direct")
    public ResponseEntity<BookingResponse> createDirectBooking(
            @RequestBody CreateBookingRequest request,
            Authentication authentication) {
        String staffId = authentication.getName();
        log.info("Staff [{}] requested direct counter booking", staffId);
        return ResponseEntity.ok(adminBookingUseCase.createDirectBooking(request, staffId));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    @PostMapping("/{id}/checkin")
    public ResponseEntity<Void> checkinBooking(@PathVariable String id) {
        log.info("Request to checkin booking with ID: {}", id);
        adminBookingUseCase.checkinBooking(id);
        return ResponseEntity.ok().build();
    }
}
