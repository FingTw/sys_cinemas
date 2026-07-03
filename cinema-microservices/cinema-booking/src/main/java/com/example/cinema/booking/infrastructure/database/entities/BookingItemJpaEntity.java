package com.example.cinema.booking.infrastructure.database.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "booking_items", schema = "booking")
@SQLDelete(sql = "UPDATE booking.booking_items SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class BookingItemJpaEntity {
    @jakarta.persistence.Column(name = "is_deleted")
    private boolean isDeleted = false;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private BookingJpaEntity booking;

    @Column(nullable = false)
    private String productId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public BookingItemJpaEntity() {
    }

    public BookingItemJpaEntity(String id, BookingJpaEntity booking, String productId, String productName, Integer quantity, BigDecimal unitPrice, LocalDateTime createdAt) {
        this.id = id;
        this.booking = booking;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.createdAt = createdAt;
    }

    public static BookingItemJpaEntityBuilder builder() {
        return new BookingItemJpaEntityBuilder();
    }

    public static class BookingItemJpaEntityBuilder {
        private String id;
        private BookingJpaEntity booking;
        private String productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private LocalDateTime createdAt;

        public BookingItemJpaEntityBuilder id(String id) {
            this.id = id;
            return this;
        }

        public BookingItemJpaEntityBuilder booking(BookingJpaEntity booking) {
            this.booking = booking;
            return this;
        }

        public BookingItemJpaEntityBuilder productId(String productId) {
            this.productId = productId;
            return this;
        }

        public BookingItemJpaEntityBuilder productName(String productName) {
            this.productName = productName;
            return this;
        }

        public BookingItemJpaEntityBuilder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public BookingItemJpaEntityBuilder unitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
            return this;
        }

        public BookingItemJpaEntityBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public BookingItemJpaEntity build() {
            return new BookingItemJpaEntity(id, booking, productId, productName, quantity, unitPrice, createdAt);
        }
    }
}
