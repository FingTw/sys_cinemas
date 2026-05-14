package com.example.cinema.booking.domain.repositories;

import com.example.cinema.booking.domain.entities.Booking;
import java.util.List;
import java.util.Optional;

public interface BookingRepository {
    Booking save(Booking booking);
    Optional<Booking> findById(String id);
    List<Booking> findAll();
    List<Booking> findByUserId(String userId);
    List<Booking> findByShowtimeId(String showtimeId);
    List<Booking> findByStatus(String status);
    boolean isAnySeatOccupied(String showtimeId, List<String> seatIds);
    void deleteById(String id);
    long countAll();
    long countByStatus(String status);
}
