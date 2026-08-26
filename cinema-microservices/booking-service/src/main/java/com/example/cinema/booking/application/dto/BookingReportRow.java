package com.example.cinema.booking.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BookingReportRow {
    private String bookingId;
    private String userId;
    private String showtimeId;
    private BigDecimal totalPrice;
    private String status;
    private String seatIds;
    private String createdAt;
    private String expiresAt;
}
