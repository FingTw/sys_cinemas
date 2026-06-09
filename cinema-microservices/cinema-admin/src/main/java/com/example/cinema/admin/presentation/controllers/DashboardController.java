package com.example.cinema.admin.presentation.controllers;

import com.example.cinema.admin.application.dto.DashboardStatsDTO;
import com.example.cinema.admin.application.ports.in.AdminBookingUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final AdminBookingUseCase adminBookingUseCase;

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        log.info("Fetching dashboard statistics");
        return ResponseEntity.ok(adminBookingUseCase.getDashboardStats());
    }
}
