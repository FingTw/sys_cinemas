package com.example.cinema.common.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingConfirmedPayload {
    private String bookingId;
    private String userId;
    private String email;
    private String movieTitle;
    private String roomName;
    private LocalDateTime showtimeStart;
    private List<SeatInfo> seats;
    private BigDecimal totalPrice;
    private String paymentTransactionId;
    private String qrCodeData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatInfo {
        private String seatId;
        private String label;
        private BigDecimal price;
    }
}
