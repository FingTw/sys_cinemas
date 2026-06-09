package com.example.cinema.admin.application.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDTO {
    private long totalUsers;
    private long totalMovies;
    private long totalShowtimes;
    private long totalRooms;

    private long totalBookings;
    private long pendingBookings;
    private long confirmedBookings;
    private BigDecimal totalRevenue;

    private long moviesShowing;
    private long moviesComingSoon;

    private long showtimesScheduled;
    private long showtimesPlaying;
    private long showtimesCompleted;

    private List<TopMovieDTO> topMovies;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopMovieDTO {
        private String movieId;
        private String movieTitle;
        private long bookingCount;
    }
}
