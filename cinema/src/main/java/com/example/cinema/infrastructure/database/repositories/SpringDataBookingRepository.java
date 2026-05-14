package com.example.cinema.infrastructure.database.repositories;

import com.example.cinema.infrastructure.database.entities.BookingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface SpringDataBookingRepository extends JpaRepository<BookingJpaEntity, String> {
    
    @Query("SELECT b FROM BookingJpaEntity b WHERE b.status = 'PENDING' AND b.expiresAt < :now")
    List<BookingJpaEntity> findExpiredPendingBookings(@Param("now") LocalDateTime now);

    List<BookingJpaEntity> findByUserId(String userId);

    List<BookingJpaEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    @Query("SELECT COUNT(bs) > 0 FROM BookingSeatJpaEntity bs " +
           "WHERE bs.showtimeId = :showtimeId AND bs.seatId IN :seatIds " +
           "AND bs.booking.status IN ('PENDING', 'CONFIRMED')")
    boolean existsByShowtimeIdAndSeatIdInAndStatusIn(
            @Param("showtimeId") String showtimeId, 
            @Param("seatIds") List<String> seatIds
    );

    long countByStatus(String status);
}

