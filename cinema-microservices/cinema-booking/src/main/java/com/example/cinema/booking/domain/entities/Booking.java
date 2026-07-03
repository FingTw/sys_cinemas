package com.example.cinema.booking.domain.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Builder;
import com.example.cinema.common.exception.ClientException;

@Getter
public class Booking {
    private String id;
    private String userId;
    private String showtimeId;
    private BigDecimal totalPrice;
    private String status; // PENDING, CONFIRMED, CANCELLED, EXPIRED
    private LocalDateTime expiresAt;
    private String paymentTransactionId;
    private LocalDateTime createdAt;
    private List<BookingSeat> seats;
    private List<BookingItem> items;

    protected Booking() {} // Cho ORM (MyBatis/JDBC)

    @Builder
    public Booking(String id, String userId, String showtimeId, BigDecimal totalPrice, String status, LocalDateTime expiresAt, String paymentTransactionId, LocalDateTime createdAt, List<BookingSeat> seats, List<BookingItem> items) {
        this.id = (id != null && !id.trim().isEmpty()) ? id : java.util.UUID.randomUUID().toString();
        this.userId = userId;
        this.showtimeId = showtimeId;
        this.totalPrice = totalPrice;
        this.status = status;
        this.expiresAt = expiresAt;
        this.paymentTransactionId = paymentTransactionId;
        this.createdAt = createdAt;
        this.seats = seats;
        this.items = items;
    }

    // --- DOMAIN BEHAVIORS ---

    /** Factory method khởi tạo Booking mới */
    public static Booking create(String userId, String showtimeId, List<BookingSeat> seats, List<BookingItem> items, int expirationMinutes) {
        if ((seats == null || seats.isEmpty()) && (items == null || items.isEmpty())) {
            throw new ClientException("Phải chọn ít nhất ghế hoặc sản phẩm.");
        }
        
        BigDecimal total = BigDecimal.ZERO;
        
        if (seats != null) {
            total = total.add(seats.stream()
                .map(BookingSeat::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        
        if (items != null) {
            total = total.add(items.stream()
                .map(BookingItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        }

        return Booking.builder()
                .userId(userId)
                .showtimeId(showtimeId)
                .totalPrice(total)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(expirationMinutes))
                .seats(seats)
                .items(items)
                .build();
    }

    /** Khách hàng xác nhận thanh toán thành công */
    public void confirmPayment(String transactionId) {
        if (!"PENDING".equals(this.status)) {
            throw new ClientException("Chỉ có thể xác nhận thanh toán cho đơn hàng đang chờ!");
        }
        this.status = "CONFIRMED";
        this.paymentTransactionId = transactionId;
    }

    /** User hủy đơn đặt vé */
    public void cancelByUser(String userIdRequesting) {
        if (this.userId != null && !this.userId.equals(userIdRequesting)) {
            throw new ClientException("Bạn không có quyền hủy đơn đặt vé này.");
        }
        if (!"PENDING".equals(this.status)) {
            throw new ClientException("Chỉ có thể hủy đơn đặt vé đang chờ thanh toán (PENDING).");
        }
        this.status = "CANCELLED";
    }

    /** Hệ thống tự động hủy đơn hết hạn */
    public void markAsExpired() {
        if (!"PENDING".equals(this.status)) {
            throw new ClientException("Trạng thái không hợp lệ để đánh dấu hết hạn.");
        }
        this.status = "EXPIRED";
    }

    /** Hoàn tiền cho đơn hàng đã thanh toán */
    public void refund() {
        if (!"CONFIRMED".equals(this.status)) {
            throw new ClientException("Đơn đặt vé chưa được thanh toán thành công (CONFIRMED) nên không thể hoàn tiền!");
        }
        this.status = "CANCELLED";
    }
}
