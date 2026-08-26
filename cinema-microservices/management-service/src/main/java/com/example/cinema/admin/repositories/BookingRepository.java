package com.example.cinema.admin.repositories;

import com.example.cinema.admin.entities.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
    List<Booking> findByStatus(String status);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(bs) > 0 FROM BookingSeat bs " +
           "WHERE bs.showtimeId = :showtimeId AND bs.seatId IN :seatIds " +
           "AND bs.booking.status IN ('PENDING', 'CONFIRMED')")
    boolean existsByShowtimeIdAndSeatIdInAndStatusIn(
            @org.springframework.data.repository.query.Param("showtimeId") String showtimeId, 
            @org.springframework.data.repository.query.Param("seatIds") List<String> seatIds
    );
    @org.springframework.data.jpa.repository.Query("SELECT b FROM Booking b WHERE b.status = 'PENDING' AND b.expiresAt < :now")
    List<Booking> findExpiredPendingBookings(@org.springframework.data.repository.query.Param("now") java.time.LocalDateTime now);

    @org.springframework.data.jpa.repository.Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Booking b JOIN b.seats s WHERE b.showtimeId = :showtimeId AND s.seatId IN :seatIds AND b.status IN ('PENDING', 'CONFIRMED')")
    boolean isAnySeatOccupied(@org.springframework.data.repository.query.Param("showtimeId") String showtimeId, @org.springframework.data.repository.query.Param("seatIds") java.util.List<String> seatIds);
}
