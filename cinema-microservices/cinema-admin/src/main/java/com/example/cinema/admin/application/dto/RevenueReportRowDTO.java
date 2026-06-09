package com.example.cinema.admin.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueReportRowDTO {
    private String movieTitle;
    private Long totalShowtimes;
    private Long totalBookings;
    private BigDecimal totalRevenue;
}
