package com.example.cinema.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {
    private String id;
    private String userId;
    private String showtimeId;
    private BigDecimal totalPrice;
    private String status;
    private LocalDateTime expiresAt;
    private List<String> seatIds;
    private String paymentUrl;
}
