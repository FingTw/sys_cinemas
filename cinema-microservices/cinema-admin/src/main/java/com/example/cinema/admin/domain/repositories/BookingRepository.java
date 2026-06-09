package com.example.cinema.admin.domain.repositories;

import com.example.cinema.admin.domain.entities.Booking;
import java.util.List;
import java.util.Optional;

public interface BookingRepository {
    Booking save(Booking booking);
    Optional<Booking> findById(String id);
    List<Booking> findAll();
    List<Booking> findByStatus(String status);
    boolean isAnySeatOccupied(String showtimeId, List<String> seatIds);
    void deleteById(String id);
}
