package com.example.cinema.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class BookingResponse {
    private String id;
    private String userId;
    private String showtimeId;
    private BigDecimal totalPrice;
    private String status;
    private LocalDateTime expiresAt;
    private List<String> seatIds;
    private String paymentUrl; // Link redirect sang VNPay

    public BookingResponse() {
    }

    public BookingResponse(String id, String userId, String showtimeId, BigDecimal totalPrice, String status, LocalDateTime expiresAt, List<String> seatIds, String paymentUrl) {
        this.id = id;
        this.userId = userId;
        this.showtimeId = showtimeId;
        this.totalPrice = totalPrice;
        this.status = status;
        this.expiresAt = expiresAt;
        this.seatIds = seatIds;
        this.paymentUrl = paymentUrl;
    }

    // Builder manual
    public static BookingResponseBuilder builder() {
        return new BookingResponseBuilder();
    }

    public static class BookingResponseBuilder {
        private String id;
        private String userId;
        private String showtimeId;
        private BigDecimal totalPrice;
        private String status;
        private LocalDateTime expiresAt;
        private List<String> seatIds;
        private String paymentUrl;

        public BookingResponseBuilder id(String id) {
            this.id = id;
            return this;
        }

        public BookingResponseBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public BookingResponseBuilder showtimeId(String showtimeId) {
            this.showtimeId = showtimeId;
            return this;
        }

        public BookingResponseBuilder totalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
            return this;
        }

        public BookingResponseBuilder status(String status) {
            this.status = status;
            return this;
        }

        public BookingResponseBuilder expiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public BookingResponseBuilder seatIds(List<String> seatIds) {
            this.seatIds = seatIds;
            return this;
        }

        public BookingResponseBuilder paymentUrl(String paymentUrl) {
            this.paymentUrl = paymentUrl;
            return this;
        }

        public BookingResponse build() {
            return new BookingResponse(id, userId, showtimeId, totalPrice, status, expiresAt, seatIds, paymentUrl);
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

    public List<String> getSeatIds() {
        return seatIds;
    }

    public void setSeatIds(List<String> seatIds) {
        this.seatIds = seatIds;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }
}
