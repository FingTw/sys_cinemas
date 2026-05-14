package com.example.cinema.domain.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Booking {
    private String id;
    private String userId;
    private String showtimeId;
    private BigDecimal totalPrice;
    private String status; // PENDING, CONFIRMED, CANCELLED, EXPIRED
    private LocalDateTime expiresAt;
    private String paymentTransactionId;
    private LocalDateTime createdAt;
    private List<BookingSeat> seats;

    public Booking() {
    }

    public Booking(String id, String userId, String showtimeId, BigDecimal totalPrice, String status, LocalDateTime expiresAt, String paymentTransactionId, LocalDateTime createdAt, List<BookingSeat> seats) {
        this.id = id;
        this.userId = userId;
        this.showtimeId = showtimeId;
        this.totalPrice = totalPrice;
        this.status = status;
        this.expiresAt = expiresAt;
        this.paymentTransactionId = paymentTransactionId;
        this.createdAt = createdAt;
        this.seats = seats;
    }

    // Builder manual
    public static BookingBuilder builder() {
        return new BookingBuilder();
    }

    public static class BookingBuilder {
        private String id;
        private String userId;
        private String showtimeId;
        private BigDecimal totalPrice;
        private String status;
        private LocalDateTime expiresAt;
        private String paymentTransactionId;
        private LocalDateTime createdAt;
        private List<BookingSeat> seats;

        public BookingBuilder id(String id) {
            this.id = id;
            return this;
        }

        public BookingBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public BookingBuilder showtimeId(String showtimeId) {
            this.showtimeId = showtimeId;
            return this;
        }

        public BookingBuilder totalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
            return this;
        }

        public BookingBuilder status(String status) {
            this.status = status;
            return this;
        }

        public BookingBuilder expiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public BookingBuilder paymentTransactionId(String paymentTransactionId) {
            this.paymentTransactionId = paymentTransactionId;
            return this;
        }

        public BookingBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public BookingBuilder seats(List<BookingSeat> seats) {
            this.seats = seats;
            return this;
        }

        public Booking build() {
            return new Booking(id, userId, showtimeId, totalPrice, status, expiresAt, paymentTransactionId, createdAt, seats);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(String showtimeId) {
        this.showtimeId = showtimeId;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getPaymentTransactionId() {
        return paymentTransactionId;
    }

    public void setPaymentTransactionId(String paymentTransactionId) {
        this.paymentTransactionId = paymentTransactionId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<BookingSeat> getSeats() {
        return seats;
    }

    public void setSeats(List<BookingSeat> seats) {
        this.seats = seats;
    }
}
