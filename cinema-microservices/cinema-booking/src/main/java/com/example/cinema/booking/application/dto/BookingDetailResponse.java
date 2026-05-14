package com.example.cinema.booking.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO chi tiết đơn đặt vé — dùng cho cả User (xem lịch sử) và Admin (quản lý).
 * Bao gồm thông tin phim, phòng, suất chiếu, danh sách ghế, trạng thái thanh toán.
 */
public class BookingDetailResponse {
    private String id;
    private String userId;
    private String username;
    private String movieTitle;
    private String roomName;
    private LocalDateTime showtimeStart;
    private LocalDateTime showtimeEnd;
    private BigDecimal totalPrice;
    private String status;
    private LocalDateTime expiresAt;
    private String paymentTransactionId;
    private LocalDateTime createdAt;
    private List<SeatInfo> seats;

    public BookingDetailResponse() {
    }

    // Inner class cho thông tin ghế
    public static class SeatInfo {
        private String seatId;
        private String rowLabel;
        private Integer colNumber;
        private String type;
        private BigDecimal price;

        public SeatInfo() {
        }

        public SeatInfo(String seatId, String rowLabel, Integer colNumber, String type, BigDecimal price) {
            this.seatId = seatId;
            this.rowLabel = rowLabel;
            this.colNumber = colNumber;
            this.type = type;
            this.price = price;
        }

        public String getSeatId() { return seatId; }
        public void setSeatId(String seatId) { this.seatId = seatId; }
        public String getRowLabel() { return rowLabel; }
        public void setRowLabel(String rowLabel) { this.rowLabel = rowLabel; }
        public Integer getColNumber() { return colNumber; }
        public void setColNumber(Integer colNumber) { this.colNumber = colNumber; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
    }

    // Builder
    public static BookingDetailResponseBuilder builder() {
        return new BookingDetailResponseBuilder();
    }

    public static class BookingDetailResponseBuilder {
        private String id;
        private String userId;
        private String username;
        private String movieTitle;
        private String roomName;
        private LocalDateTime showtimeStart;
        private LocalDateTime showtimeEnd;
        private BigDecimal totalPrice;
        private String status;
        private LocalDateTime expiresAt;
        private String paymentTransactionId;
        private LocalDateTime createdAt;
        private List<SeatInfo> seats;

        public BookingDetailResponseBuilder id(String id) { this.id = id; return this; }
        public BookingDetailResponseBuilder userId(String userId) { this.userId = userId; return this; }
        public BookingDetailResponseBuilder username(String username) { this.username = username; return this; }
        public BookingDetailResponseBuilder movieTitle(String movieTitle) { this.movieTitle = movieTitle; return this; }
        public BookingDetailResponseBuilder roomName(String roomName) { this.roomName = roomName; return this; }
        public BookingDetailResponseBuilder showtimeStart(LocalDateTime showtimeStart) { this.showtimeStart = showtimeStart; return this; }
        public BookingDetailResponseBuilder showtimeEnd(LocalDateTime showtimeEnd) { this.showtimeEnd = showtimeEnd; return this; }
        public BookingDetailResponseBuilder totalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; return this; }
        public BookingDetailResponseBuilder status(String status) { this.status = status; return this; }
        public BookingDetailResponseBuilder expiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; return this; }
        public BookingDetailResponseBuilder paymentTransactionId(String paymentTransactionId) { this.paymentTransactionId = paymentTransactionId; return this; }
        public BookingDetailResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public BookingDetailResponseBuilder seats(List<SeatInfo> seats) { this.seats = seats; return this; }

        public BookingDetailResponse build() {
            BookingDetailResponse r = new BookingDetailResponse();
            r.id = id; r.userId = userId; r.username = username;
            r.movieTitle = movieTitle; r.roomName = roomName;
            r.showtimeStart = showtimeStart; r.showtimeEnd = showtimeEnd;
            r.totalPrice = totalPrice; r.status = status;
            r.expiresAt = expiresAt; r.paymentTransactionId = paymentTransactionId;
            r.createdAt = createdAt; r.seats = seats;
            return r;
        }
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public LocalDateTime getShowtimeStart() { return showtimeStart; }
    public void setShowtimeStart(LocalDateTime showtimeStart) { this.showtimeStart = showtimeStart; }
    public LocalDateTime getShowtimeEnd() { return showtimeEnd; }
    public void setShowtimeEnd(LocalDateTime showtimeEnd) { this.showtimeEnd = showtimeEnd; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public String getPaymentTransactionId() { return paymentTransactionId; }
    public void setPaymentTransactionId(String paymentTransactionId) { this.paymentTransactionId = paymentTransactionId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<SeatInfo> getSeats() { return seats; }
    public void setSeats(List<SeatInfo> seats) { this.seats = seats; }
}
