package com.example.cinema.booking.application.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO chứa toàn bộ dữ liệu thống kê cho Admin Dashboard.
 */
public class DashboardStatsDTO {

    // Tổng quan
    private long totalUsers;
    private long totalMovies;
    private long totalShowtimes;
    private long totalRooms;

    // Thống kê Booking
    private long totalBookings;
    private long pendingBookings;
    private long confirmedBookings;
    private BigDecimal totalRevenue;

    // Phim theo trạng thái
    private long moviesShowing;
    private long moviesComingSoon;

    // Suất chiếu theo trạng thái
    private long showtimesScheduled;
    private long showtimesPlaying;
    private long showtimesCompleted;

    // Top phim (theo số lượng booking)
    private List<TopMovieDTO> topMovies;

    public DashboardStatsDTO() {
    }

    // Inner class cho top movies
    public static class TopMovieDTO {
        private String movieId;
        private String movieTitle;
        private long bookingCount;

        public TopMovieDTO() {
        }

        public TopMovieDTO(String movieId, String movieTitle, long bookingCount) {
            this.movieId = movieId;
            this.movieTitle = movieTitle;
            this.bookingCount = bookingCount;
        }

        public String getMovieId() { return movieId; }
        public void setMovieId(String movieId) { this.movieId = movieId; }
        public String getMovieTitle() { return movieTitle; }
        public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }
        public long getBookingCount() { return bookingCount; }
        public void setBookingCount(long bookingCount) { this.bookingCount = bookingCount; }
    }

    // Getters & Setters
    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
    public long getTotalMovies() { return totalMovies; }
    public void setTotalMovies(long totalMovies) { this.totalMovies = totalMovies; }
    public long getTotalShowtimes() { return totalShowtimes; }
    public void setTotalShowtimes(long totalShowtimes) { this.totalShowtimes = totalShowtimes; }
    public long getTotalRooms() { return totalRooms; }
    public void setTotalRooms(long totalRooms) { this.totalRooms = totalRooms; }
    public long getTotalBookings() { return totalBookings; }
    public void setTotalBookings(long totalBookings) { this.totalBookings = totalBookings; }
    public long getPendingBookings() { return pendingBookings; }
    public void setPendingBookings(long pendingBookings) { this.pendingBookings = pendingBookings; }
    public long getConfirmedBookings() { return confirmedBookings; }
    public void setConfirmedBookings(long confirmedBookings) { this.confirmedBookings = confirmedBookings; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    public long getMoviesShowing() { return moviesShowing; }
    public void setMoviesShowing(long moviesShowing) { this.moviesShowing = moviesShowing; }
    public long getMoviesComingSoon() { return moviesComingSoon; }
    public void setMoviesComingSoon(long moviesComingSoon) { this.moviesComingSoon = moviesComingSoon; }
    public long getShowtimesScheduled() { return showtimesScheduled; }
    public void setShowtimesScheduled(long showtimesScheduled) { this.showtimesScheduled = showtimesScheduled; }
    public long getShowtimesPlaying() { return showtimesPlaying; }
    public void setShowtimesPlaying(long showtimesPlaying) { this.showtimesPlaying = showtimesPlaying; }
    public long getShowtimesCompleted() { return showtimesCompleted; }
    public void setShowtimesCompleted(long showtimesCompleted) { this.showtimesCompleted = showtimesCompleted; }
    public List<TopMovieDTO> getTopMovies() { return topMovies; }
    public void setTopMovies(List<TopMovieDTO> topMovies) { this.topMovies = topMovies; }
}
