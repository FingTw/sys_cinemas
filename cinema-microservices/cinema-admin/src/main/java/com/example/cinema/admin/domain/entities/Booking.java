package com.example.cinema.admin.domain.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
public class Booking {
    private String id;
    private String userId;
    private String showtimeId;
    private BigDecimal totalPrice;
    private String status; // PENDING, CONFIRMED, CANCELLED, EXPIRED
    private boolean checkedIn;
    private LocalDateTime expiresAt;
    private String paymentTransactionId;
    private LocalDateTime createdAt;
    private List<BookingSeat> seats;

    @Builder
    public Booking(String id, String userId, String showtimeId, BigDecimal totalPrice, String status, boolean checkedIn, LocalDateTime expiresAt, String paymentTransactionId, LocalDateTime createdAt, List<BookingSeat> seats) {
        this.id = (id != null && !id.trim().isEmpty()) ? id : java.util.UUID.randomUUID().toString();
        this.userId = userId;
        this.showtimeId = showtimeId;
        this.totalPrice = totalPrice;
        this.status = status;
        this.checkedIn = checkedIn;
        this.expiresAt = expiresAt;
        this.paymentTransactionId = paymentTransactionId;
        this.createdAt = createdAt;
        this.seats = seats;
    }

    public void updateStatus(String status) {
        this.status = status;
    }

    public void updateCheckIn(boolean checkedIn) {
        this.checkedIn = checkedIn;
    }
}
