package com.example.cinema.booking.infrastructure.database.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bookings", schema = "booking", indexes = {
    @Index(name = "idx_booking_user", columnList = "userId"),
    @Index(name = "idx_booking_showtime", columnList = "showtimeId")
})
@SQLDelete(sql = "UPDATE booking.bookings SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class BookingJpaEntity {
    @jakarta.persistence.Column(name = "is_deleted")
    private boolean isDeleted = false;

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
}
