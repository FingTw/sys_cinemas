package com.example.cinema.booking.domain.entities;

import java.math.BigDecimal;

public class BookingSeat {
    private String id;
    private String bookingId;
    private String seatId;
    private String showtimeId; // Luu de validate unique tren DB
    private BigDecimal price;

    public BookingSeat() {
    }

    public BookingSeat(String id, String bookingId, String seatId, String showtimeId, BigDecimal price) {
        this.id = id;
        this.bookingId = bookingId;
        this.seatId = seatId;
        this.showtimeId = showtimeId;
        this.price = price;
    }

    // Builder manual
    public static BookingSeatBuilder builder() {
        return new BookingSeatBuilder();
    }

    public static class BookingSeatBuilder {
        private String id;
        private String bookingId;
        private String seatId;
        private String showtimeId;
        private BigDecimal price;

        public BookingSeatBuilder id(String id) {
            this.id = id;
            return this;
        }

        public BookingSeatBuilder bookingId(String bookingId) {
            this.bookingId = bookingId;
            return this;
        }

        public BookingSeatBuilder seatId(String seatId) {
            this.seatId = seatId;
            return this;
        }

        public BookingSeatBuilder showtimeId(String showtimeId) {
            this.showtimeId = showtimeId;
            return this;
        }

        public BookingSeatBuilder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public BookingSeat build() {
            return new BookingSeat(id, bookingId, seatId, showtimeId, price);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getSeatId() {
        return seatId;
    }

    public void setSeatId(String seatId) {
        this.seatId = seatId;
    }

    public String getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(String showtimeId) {
        this.showtimeId = showtimeId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
