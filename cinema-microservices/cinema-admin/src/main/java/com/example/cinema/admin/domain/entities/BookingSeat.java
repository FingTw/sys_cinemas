package com.example.cinema.admin.domain.entities;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
public class BookingSeat {
    private String id;
    private String bookingId;
    private String seatId;
    private String showtimeId;
    private BigDecimal price;

    @Builder
    public BookingSeat(String id, String bookingId, String seatId, String showtimeId, BigDecimal price) {
        this.id = (id != null && !id.trim().isEmpty()) ? id : java.util.UUID.randomUUID().toString();
        this.bookingId = bookingId;
        this.seatId = seatId;
        this.showtimeId = showtimeId;
        this.price = price;
    }
}
