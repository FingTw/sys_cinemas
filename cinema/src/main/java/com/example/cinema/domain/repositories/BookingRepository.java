package com.example.cinema.domain.repositories;

import com.example.cinema.domain.entities.Booking;
import java.util.Optional;
import java.util.List;

public interface BookingRepository {
    Booking save(Booking booking);
    Optional<Booking> findById(String id);
    List<Booking> findByUserId(String userId);
    List<Booking> findAll();
    void deleteById(String id);
    
    // Tìm các booking đã hết hạn nhưng vẫn ở trạng thái PENDING
    List<Booking> findExpiredPendingBookings();
    
    // Kiểm tra xem các ghế có bị trùng không
    boolean isAnySeatOccupied(String showtimeId, List<String> seatIds);

    // Thống kê cho Dashboard
    long countByStatus(String status);
    long countAll();
}
