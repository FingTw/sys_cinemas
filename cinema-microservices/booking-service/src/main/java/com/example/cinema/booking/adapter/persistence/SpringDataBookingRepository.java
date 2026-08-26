package com.example.cinema.booking.adapter.persistence;

import com.example.cinema.booking.adapter.persistence.entity.BookingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface SpringDataBookingRepository extends JpaRepository<BookingJpaEntity, String> {
    
    @Query("SELECT b FROM BookingJpaEntity b WHERE b.id = :id")
    java.util.Optional<BookingJpaEntity> findBookingById(@Param("id") String id);

    @Query("SELECT b FROM BookingJpaEntity b WHERE b.status = 'PENDING' AND b.expiresAt < :now")
    List<BookingJpaEntity> findExpiredPendingBookings(@Param("now") LocalDateTime now);

    List<BookingJpaEntity> findByUserId(String userId);

    List<BookingJpaEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    long countByStatus(String status);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM BookingJpaEntity b JOIN b.seats bs WHERE b.showtimeId = :showtimeId AND bs.seatId IN :seatIds AND b.status IN ('PENDING', 'CONFIRMED')")
    boolean isAnySeatOccupied(@Param("showtimeId") String showtimeId, @Param("seatIds") List<String> seatIds);

    @Query("SELECT bs.seatId FROM BookingJpaEntity b JOIN b.seats bs WHERE b.showtimeId = :showtimeId AND b.status IN ('PENDING', 'CONFIRMED')")
    List<String> findOccupiedSeatIdsByShowtime(@Param("showtimeId") String showtimeId);
}
