package com.example.cinema.booking.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

/**
 * DTO chi tiết đơn đặt vé — dùng cho cả User (xem lịch sử) và Admin (quản lý).
 * Bao gồm thông tin phim, phòng, suất chiếu, danh sách ghế, trạng thái thanh toán.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDetailResponse {
    private String id;
    private String userId;
    private String username;
    private String showtimeId;
    private String movieId;
    private String movieTitle;
    private String roomName;
    private LocalDateTime showtimeStart;
    private LocalDateTime showtimeEnd;
    private BigDecimal totalPrice;
    private String status;
    private LocalDateTime expiresAt;
    private String paymentTransactionId;
    private LocalDateTime createdAt;
    private List<SeatInfo> seats;

    /**
     * Inner class cho thông tin ghế
     */
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
