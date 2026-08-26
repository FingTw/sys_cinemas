package com.example.cinema.booking.application.port;

import com.example.cinema.booking.domain.Booking;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepositoryPort {
    Optional<Booking> findById(String id);
    Booking save(Booking booking);
    List<Booking> findExpiredPendingBookings(LocalDateTime now);
    List<Booking> findByUserId(String userId);
    List<Booking> findByUserIdOrderByCreatedAtDesc(String userId);
    boolean isAnySeatOccupied(String showtimeId, List<String> seatIds);
    List<String> findOccupiedSeatIdsByShowtime(String showtimeId);
    long countByStatus(String status);
}
