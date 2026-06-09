package com.example.cinema.booking.domain.entities;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Builder;
import com.example.cinema.common.exception.ClientException;

@Getter
public class BookingSeat {
    private String id;
    private String bookingId;
    private String seatId;
    private String showtimeId;
    private BigDecimal price;

    protected BookingSeat() {} // Dành cho ORM nếu cần

    @Builder
    public BookingSeat(String id, String bookingId, String seatId, String showtimeId, BigDecimal price) {
        if (price != null && price.compareTo(BigDecimal.ZERO) < 0) {
            throw new ClientException("Giá ghế không được âm!");
        }
        this.id = (id != null && !id.trim().isEmpty()) ? id : java.util.UUID.randomUUID().toString();
        this.bookingId = bookingId;
        this.seatId = seatId;
        this.showtimeId = showtimeId;
        this.price = price;
    }
}
