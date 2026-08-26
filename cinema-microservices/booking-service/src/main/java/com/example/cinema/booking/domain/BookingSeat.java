package com.example.cinema.booking.domain;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingSeat {
    private boolean isDeleted = false;
    private String id;
    private Booking booking;
    private String seatId;
    private String showtimeId;
    private BigDecimal price;

    public BookingSeat() {
    }

    public BookingSeat(String id, Booking booking, String seatId, String showtimeId, BigDecimal price) {
        this.id = id;
        this.booking = booking;
        this.seatId = seatId;
        this.showtimeId = showtimeId;
        this.price = price;
    }

    public static BookingSeatBuilder builder() {
        return new BookingSeatBuilder();
    }

    public static class BookingSeatBuilder {
        private String id;
        private Booking booking;
        private String seatId;
        private String showtimeId;
        private BigDecimal price;

        public BookingSeatBuilder id(String id) {
            this.id = id;
            return this;
        }

        public BookingSeatBuilder booking(Booking booking) {
            this.booking = booking;
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
            return new BookingSeat(id, booking, seatId, showtimeId, price);
        }
    }
}
