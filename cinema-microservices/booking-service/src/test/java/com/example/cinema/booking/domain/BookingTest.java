package com.example.cinema.booking.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit test cho Booking domain object.
 *
 * TIÊU CHUẨN:
 * - Không dùng Spring context → khởi động cực nhanh (<1s)
 * - Không mock gì cả → test logic thuần túy của domain
 * - Tên test theo pattern: methodName_condition_expectedResult
 * - Mỗi test chỉ assert 1 thứ (Single Responsibility)
 */
@DisplayName("Booking Domain")
class BookingTest {

    // ─── Test data builders (tránh lặp code) ───────────────────
    private static BookingSeat makeSeat(String seatId, BigDecimal price) {
        return BookingSeat.builder()
                .seatId(seatId)
                .showtimeId("showtime-001")
                .price(price)
                .build();
    }

    // ═══════════════════════════════════════════════════════════
    // Booking.create()
    // ═══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("Booking.create()")
    class CreateBooking {

        @Test
        @DisplayName("Khi tạo booking thì status phải là PENDING")
        void create_always_statusIsPending() {
            var booking = Booking.create("user-1", "show-1", List.of(), List.of(), 5);
            assertThat(booking.getStatus()).isEqualTo("PENDING");
        }

        @Test
        @DisplayName("Khi tạo booking thì expiresAt phải sau thời điểm hiện tại")
        void create_withExpireMinutes_expiresAtIsInFuture() {
            var before = LocalDateTime.now();
            var booking = Booking.create("user-1", "show-1", List.of(), List.of(), 5);
            assertThat(booking.getExpiresAt()).isAfter(before);
        }

        @Test
        @DisplayName("totalPrice phải bằng tổng giá tất cả các ghế")
        void create_withSeats_totalPriceEqualsSum() {
            var seat1 = makeSeat("A1", new BigDecimal("75000"));
            var seat2 = makeSeat("A2", new BigDecimal("120000"));

            var booking = Booking.create("user-1", "show-1", List.of(seat1, seat2), List.of(), 5);

            assertThat(booking.getTotalPrice()).isEqualByComparingTo(new BigDecimal("195000"));
        }

        @Test
        @DisplayName("totalPrice bằng 0 khi không có ghế")
        void create_withNoSeats_totalPriceIsZero() {
            var booking = Booking.create("user-1", "show-1", List.of(), List.of(), 5);
            assertThat(booking.getTotalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Các seat phải được gắn tham chiếu ngược về booking")
        void create_withSeats_seatsHaveBookingReference() {
            var seat = makeSeat("A1", new BigDecimal("75000"));
            var booking = Booking.create("user-1", "show-1", List.of(seat), List.of(), 5);
            assertThat(seat.getBooking()).isSameAs(booking);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // Booking.confirmPayment()
    // ═══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("Booking.confirmPayment()")
    class ConfirmPayment {

        @Test
        @DisplayName("Sau khi confirm thì status phải là CONFIRMED")
        void confirmPayment_validTransaction_statusIsConfirmed() {
            var booking = Booking.create("user-1", "show-1", List.of(), List.of(), 5);
            booking.confirmPayment("VNP-TXN-123");
            assertThat(booking.getStatus()).isEqualTo("CONFIRMED");
        }

        @Test
        @DisplayName("Sau khi confirm thì transactionId phải được lưu")
        void confirmPayment_validTransaction_transactionIdSaved() {
            var booking = Booking.create("user-1", "show-1", List.of(), List.of(), 5);
            booking.confirmPayment("VNP-TXN-123");
            assertThat(booking.getPaymentTransactionId()).isEqualTo("VNP-TXN-123");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // Booking.markAsExpired() / refund() / cancelByUser()
    // ═══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("Booking state transitions")
    class StateTransitions {

        @Test
        @DisplayName("markAsExpired() phải đổi status thành EXPIRED")
        void markAsExpired_always_statusIsExpired() {
            var booking = Booking.create("user-1", "show-1", List.of(), List.of(), 5);
            booking.markAsExpired();
            assertThat(booking.getStatus()).isEqualTo("EXPIRED");
        }

        @Test
        @DisplayName("refund() phải đổi status thành CANCELLED")
        void refund_always_statusIsCancelled() {
            var booking = Booking.create("user-1", "show-1", List.of(), List.of(), 5);
            booking.confirmPayment("TXN-1"); // phải confirm trước rồi mới refund
            booking.refund();
            assertThat(booking.getStatus()).isEqualTo("CANCELLED");
        }

        @Test
        @DisplayName("cancelByUser() phải đổi status thành CANCELLED")
        void cancelByUser_always_statusIsCancelled() {
            var booking = Booking.create("user-1", "show-1", List.of(), List.of(), 5);
            booking.cancelByUser("user-1");
            assertThat(booking.getStatus()).isEqualTo("CANCELLED");
        }
    }
}
