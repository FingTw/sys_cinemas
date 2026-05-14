package com.example.cinema.application.ports.in;

import com.example.cinema.application.dto.DashboardStatsDTO;

/**
 * Port cho dữ liệu Dashboard thống kê dành cho Admin.
 */
public interface DashboardQueryUseCase {
    DashboardStatsDTO getDashboardStats();
}
