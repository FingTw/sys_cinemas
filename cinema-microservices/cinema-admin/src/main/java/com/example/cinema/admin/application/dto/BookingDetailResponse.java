package com.example.cinema.admin.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDetailResponse {
    private String id;
    private String userId;
    private String username;
    private String movieTitle;
    private String roomName;
    private LocalDateTime showtimeStart;
    private LocalDateTime showtimeEnd;
    private BigDecimal totalPrice;
    private String status;
    private boolean checkedIn;
    private LocalDateTime expiresAt;
    private String paymentTransactionId;
    private LocalDateTime createdAt;
    private List<SeatInfo> seats;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SeatInfo {
        private String seatId;
        private String rowLabel;
        private Integer colNumber;
        private String type;
        private BigDecimal price;
    }
}
