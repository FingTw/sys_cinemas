package com.example.cinema.booking.adapter.scheduler;

import com.example.cinema.booking.domain.Booking;
import com.example.cinema.booking.application.port.BookingRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class BookingCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(BookingCleanupScheduler.class);

    private final BookingRepositoryPort bookingRepository;

    public BookingCleanupScheduler(BookingRepositoryPort bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    /**
     * Chạy mỗi phút để quét và dọn dẹp các booking PENDING đã hết hạn.
     * (Vô hiệu hóa vì đã có Camunda Timer Boundary Event tự động dọn dẹp).
     */
    // @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupExpiredBookings() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> expiredBookings = bookingRepository.findExpiredPendingBookings(now);

        if (expiredBookings.isEmpty()) {
            return;
        }

        log.info("Phát hiện {} đơn đặt vé (PENDING) đã quá hạn thanh toán. Tiến hành dọn dẹp...", expiredBookings.size());

        for (Booking booking : expiredBookings) {
            try {
                log.info("Dọn dẹp Booking quá hạn: ID [{}], Hết hạn lúc: [{}]", booking.getId(), booking.getExpiresAt());
                booking.setStatus("EXPIRED");
                
                // Giải phóng ghế bằng cách xóa các dòng booking_seats liên quan
                // Nhờ cascade = CascadeType.ALL và orphanRemoval = true, khi clear list seats,
                // JPA sẽ tự động chạy câu lệnh DELETE FROM booking_seats tương ứng.
                if (booking.getSeats() != null) {
                    booking.getSeats().clear();
                }
                
                bookingRepository.save(booking);
            } catch (Exception e) {
                log.error("Lỗi khi dọn dẹp Booking quá hạn [{}]: {}", booking.getId(), e.getMessage(), e);
            }
        }
    }
}
