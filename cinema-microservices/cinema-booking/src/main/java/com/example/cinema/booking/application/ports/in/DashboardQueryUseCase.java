package com.example.cinema.booking.application.ports.in;

import com.example.cinema.booking.application.dto.DashboardStatsDTO;

/**
 * Port cho dữ liệu Dashboard thống kê dành cho Admin.
 */
public interface DashboardQueryUseCase {
    DashboardStatsDTO getDashboardStats();
}
