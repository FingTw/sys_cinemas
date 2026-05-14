package com.example.cinema.infrastructure.database.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "bookings", schema = "booking")
public class BookingJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String showtimeId;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, CONFIRMED, CANCELLED, EXPIRED

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private String paymentTransactionId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingSeatJpaEntity> seats;

    public BookingJpaEntity() {
    }

    public BookingJpaEntity(String id, String userId, String showtimeId, BigDecimal totalPrice, String status, LocalDateTime expiresAt, String paymentTransactionId, LocalDateTime createdAt, List<BookingSeatJpaEntity> seats) {
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
    public static BookingJpaEntityBuilder builder() {
        return new BookingJpaEntityBuilder();
    }

    public static class BookingJpaEntityBuilder {
        private String id;
        private String userId;
        private String showtimeId;
        private BigDecimal totalPrice;
        private String status = "PENDING";
        private LocalDateTime expiresAt;
        private String paymentTransactionId;
        private LocalDateTime createdAt;
        private List<BookingSeatJpaEntity> seats;

        public BookingJpaEntityBuilder id(String id) {
            this.id = id;
            return this;
        }

        public BookingJpaEntityBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public BookingJpaEntityBuilder showtimeId(String showtimeId) {
            this.showtimeId = showtimeId;
            return this;
        }

        public BookingJpaEntityBuilder totalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
            return this;
        }

        public BookingJpaEntityBuilder status(String status) {
            this.status = status;
            return this;
        }

        public BookingJpaEntityBuilder expiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public BookingJpaEntityBuilder paymentTransactionId(String paymentTransactionId) {
            this.paymentTransactionId = paymentTransactionId;
            return this;
        }

        public BookingJpaEntityBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public BookingJpaEntityBuilder seats(List<BookingSeatJpaEntity> seats) {
            this.seats = seats;
            return this;
        }

        public BookingJpaEntity build() {
            return new BookingJpaEntity(id, userId, showtimeId, totalPrice, status, expiresAt, paymentTransactionId, createdAt, seats);
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

    public List<BookingSeatJpaEntity> getSeats() {
        return seats;
    }

    public void setSeats(List<BookingSeatJpaEntity> seats) {
        this.seats = seats;
    }
}
