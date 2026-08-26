package com.example.cinema.booking.application.dto;

import java.math.BigDecimal;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatStatusDTO {
    private String seatId;
    private String rowLabel;
    private Integer colNumber;
    private String type;
    private String status; // AVAILABLE, HELD (Dang cho thanh toan), SOLD (Da mua)
    private BigDecimal price;
}
