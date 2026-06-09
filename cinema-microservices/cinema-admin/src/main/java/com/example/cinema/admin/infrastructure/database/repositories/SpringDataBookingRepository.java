package com.example.cinema.admin.infrastructure.database.repositories;

import com.example.cinema.admin.infrastructure.database.entities.BookingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SpringDataBookingRepository extends JpaRepository<BookingJpaEntity, String> {
    List<BookingJpaEntity> findByStatus(String status);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(bs) > 0 FROM BookingSeatJpaEntity bs " +
           "WHERE bs.showtimeId = :showtimeId AND bs.seatId IN :seatIds " +
           "AND bs.booking.status IN ('PENDING', 'CONFIRMED')")
    boolean existsByShowtimeIdAndSeatIdInAndStatusIn(
            @org.springframework.data.repository.query.Param("showtimeId") String showtimeId, 
            @org.springframework.data.repository.query.Param("seatIds") List<String> seatIds
    );
    @org.springframework.data.jpa.repository.Query("SELECT b FROM BookingJpaEntity b WHERE b.status = 'PENDING' AND b.expiresAt < :now")
    List<BookingJpaEntity> findExpiredPendingBookings(@org.springframework.data.repository.query.Param("now") java.time.LocalDateTime now);
}
