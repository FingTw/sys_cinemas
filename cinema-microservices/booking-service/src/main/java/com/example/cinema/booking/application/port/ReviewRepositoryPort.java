package com.example.cinema.booking.application.port;

import com.example.cinema.booking.domain.Review;
import java.util.List;
import java.util.Optional;

public interface ReviewRepositoryPort {
    Review save(Review review);
    Optional<Review> findByBookingId(String bookingId);
    List<Review> findByMovieId(String movieId);
    List<Review> findByUserId(String userId);
    boolean existsByBookingId(String bookingId);
}
