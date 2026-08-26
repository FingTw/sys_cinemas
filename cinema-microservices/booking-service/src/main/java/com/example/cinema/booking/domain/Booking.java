package com.example.cinema.booking.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Booking {
    private boolean isDeleted = false;
    private String id;
    private String userId;
    private String showtimeId;
    private BigDecimal totalPrice;
    private String status = "PENDING"; // PENDING, CONFIRMED, CANCELLED, EXPIRED
    private LocalDateTime expiresAt;
    private String paymentTransactionId;
    private LocalDateTime createdAt;
    private List<BookingSeat> seats;
    private List<BookingItem> items;

    public Booking() {
    }

    public static Booking create(String userId, String showtimeId, List<BookingSeat> seats, List<BookingItem> items, int expireMinutes) {
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setShowtimeId(showtimeId);
        booking.setSeats(seats);
        if(seats != null) {
            for(BookingSeat s : seats) s.setBooking(booking);
        }
        booking.setItems(items);
        if(items != null) {
            for(BookingItem i : items) i.setBooking(booking);
        }
        
        BigDecimal total = BigDecimal.ZERO;
        if(seats != null) {
            for(BookingSeat s : seats) total = total.add(s.getPrice());
        }
        if(items != null) {
            for(BookingItem i : items) total = total.add(i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())));
        }
        booking.setTotalPrice(total);
        booking.setStatus("PENDING");
        booking.setExpiresAt(LocalDateTime.now().plusMinutes(expireMinutes));
        return booking;
    }

    public void confirmPayment(String transactionId) {
        this.status = "CONFIRMED";
        this.paymentTransactionId = transactionId;
    }

    public void markAsExpired() {
        this.status = "EXPIRED";
    }

    public void refund() {
        this.status = "CANCELLED";
    }

    public void cancelByUser(String userId) {
        this.status = "CANCELLED";
    }

    public Booking(String id, String userId, String showtimeId, BigDecimal totalPrice, String status, LocalDateTime expiresAt, String paymentTransactionId, LocalDateTime createdAt, List<BookingSeat> seats, List<BookingItem> items) {
        this.id = id;
        this.userId = userId;
        this.showtimeId = showtimeId;
        this.totalPrice = totalPrice;
        this.status = status;
        this.expiresAt = expiresAt;
        this.paymentTransactionId = paymentTransactionId;
        this.createdAt = createdAt;
        this.seats = seats;
        this.items = items;
    }

    public static BookingBuilder builder() {
        return new BookingBuilder();
    }

    public static class BookingBuilder {
        private String id;
        private String userId;
        private String showtimeId;
        private BigDecimal totalPrice;
        private String status = "PENDING";
        private LocalDateTime expiresAt;
        private String paymentTransactionId;
        private LocalDateTime createdAt;
        private List<BookingSeat> seats;
        private List<BookingItem> items;

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

        public BookingBuilder items(List<BookingItem> items) {
            this.items = items;
            return this;
        }

        public Booking build() {
            return new Booking(id, userId, showtimeId, totalPrice, status, expiresAt, paymentTransactionId, createdAt, seats, items);
        }
    }
}
