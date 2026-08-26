package com.example.cinema.booking.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingItem {
    private boolean isDeleted = false;
    private String id;
    private Booking booking;
    private String productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private LocalDateTime createdAt;

    public BookingItem() {
    }

    public BigDecimal getTotalPrice() {
        if(unitPrice == null) return BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public BookingItem(String id, Booking booking, String productId, String productName, Integer quantity, BigDecimal unitPrice, LocalDateTime createdAt) {
        this.id = id;
        this.booking = booking;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.createdAt = createdAt;
    }

    public static BookingItemBuilder builder() {
        return new BookingItemBuilder();
    }

    public static class BookingItemBuilder {
        private String id;
        private Booking booking;
        private String productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private LocalDateTime createdAt;

        public BookingItemBuilder id(String id) {
            this.id = id;
            return this;
        }

        public BookingItemBuilder booking(Booking booking) {
            this.booking = booking;
            return this;
        }

        public BookingItemBuilder productId(String productId) {
            this.productId = productId;
            return this;
        }

        public BookingItemBuilder productName(String productName) {
            this.productName = productName;
            return this;
        }

        public BookingItemBuilder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public BookingItemBuilder unitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
            return this;
        }

        public BookingItemBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public BookingItem build() {
            return new BookingItem(id, booking, productId, productName, quantity, unitPrice, createdAt);
        }
    }
}
