package com.example.cinema.infrastructure.scheduling;

import com.example.cinema.domain.entities.Booking;
import com.example.cinema.domain.repositories.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class BookingCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(BookingCleanupScheduler.class);

    private final BookingRepository bookingRepository;

    public BookingCleanupScheduler(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    /**
     * Chay moi 30 giay de quet cac booking het han chua thanh toan
     */
    @Scheduled(fixedRate = 30000)
    @Transactional
    public void cleanupExpiredBookings() {
        List<Booking> expiredBookings = bookingRepository.findExpiredPendingBookings();

        if (!expiredBookings.isEmpty()) {
            log.info(" Dang don dep {} don dat ve het han...", expiredBookings.size());
            for (Booking booking : expiredBookings) {
                // Chung ta co the doi status thanh EXPIRED hoac xoa han de nha ghe
                // O day toi chon xoa han de nha rang buoc UNIQUE uk_seat_showtime tren DB
                bookingRepository.deleteById(booking.getId());
                log.info("Da nha ghe cho Booking ID: [{}] do het thoi gian thanh toan", booking.getId());
            }
        }
    }
}
