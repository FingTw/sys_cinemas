package com.example.cinema.booking.adapter.persistence;

import com.example.cinema.booking.adapter.persistence.entity.ReviewJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SpringDataReviewRepository extends JpaRepository<ReviewJpaEntity, String> {
    Optional<ReviewJpaEntity> findByBookingId(String bookingId);
    List<ReviewJpaEntity> findByMovieIdOrderByCreatedAtDesc(String movieId);
    List<ReviewJpaEntity> findByUserIdOrderByCreatedAtDesc(String userId);
    boolean existsByBookingId(String bookingId);
}
