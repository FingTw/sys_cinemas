package com.example.cinema.admin.infrastructure.scheduler;

import com.example.cinema.admin.infrastructure.database.entities.BookingJpaEntity;
import com.example.cinema.admin.infrastructure.database.repositories.SpringDataBookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AdminBookingCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(AdminBookingCleanupScheduler.class);

    private final SpringDataBookingRepository bookingRepository;

    public AdminBookingCleanupScheduler(SpringDataBookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    /**
     * Chạy mỗi phút để quét và dọn dẹp các booking PENDING đã hết hạn.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupExpiredBookings() {
        LocalDateTime now = LocalDateTime.now();
        List<BookingJpaEntity> expiredBookings = bookingRepository.findExpiredPendingBookings(now);

        if (expiredBookings.isEmpty()) {
            return;
        }

        log.info("[Admin] Phát hiện {} đơn đặt vé (PENDING) đã quá hạn thanh toán. Tiến hành dọn dẹp...", expiredBookings.size());

        for (BookingJpaEntity booking : expiredBookings) {
            try {
                log.info("[Admin] Dọn dẹp Booking quá hạn: ID [{}], Hết hạn lúc: [{}]", booking.getId(), booking.getExpiresAt());
                booking.setStatus("EXPIRED");
                
                // Giải phóng ghế bằng cách xóa các dòng booking_seats liên quan
                if (booking.getSeats() != null) {
                    booking.getSeats().clear();
                }
                
                bookingRepository.save(booking);
            } catch (Exception e) {
                log.error("[Admin] Lỗi khi dọn dẹp Booking quá hạn [{}]: {}", booking.getId(), e.getMessage(), e);
            }
        }
    }
}
