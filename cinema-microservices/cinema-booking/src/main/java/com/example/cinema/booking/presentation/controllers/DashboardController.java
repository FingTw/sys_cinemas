package com.example.cinema.booking.presentation.controllers;

import com.example.cinema.booking.application.dto.DashboardStatsDTO;
import com.example.cinema.booking.application.ports.in.DashboardQueryUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller cung cấp dữ liệu thống kê cho Admin Dashboard.
 * Endpoint: /api/v1/admin/dashboard
 */
@RestController
@RequestMapping("/api/v1/admin/dashboard")
public class DashboardController {

    private final DashboardQueryUseCase dashboardQueryUseCase;

    public DashboardController(DashboardQueryUseCase dashboardQueryUseCase) {
        this.dashboardQueryUseCase = dashboardQueryUseCase;
    }

    /**
     * Lấy toàn bộ dữ liệu thống kê Dashboard.
     */
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        return ResponseEntity.ok(dashboardQueryUseCase.getDashboardStats());
    }
}
