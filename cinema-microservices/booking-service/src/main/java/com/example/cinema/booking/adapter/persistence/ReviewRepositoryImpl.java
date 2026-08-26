package com.example.cinema.booking.adapter.persistence;

import com.example.cinema.booking.adapter.persistence.entity.ReviewJpaEntity;
import com.example.cinema.booking.application.port.ReviewRepositoryPort;
import com.example.cinema.booking.domain.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryPort {

    private final SpringDataReviewRepository jpaRepository;

    @Override
    public Review save(Review review) {
        ReviewJpaEntity entity = toJpa(review);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Review> findByBookingId(String bookingId) {
        return jpaRepository.findByBookingId(bookingId).map(this::toDomain);
    }

    @Override
    public List<Review> findByMovieId(String movieId) {
        return jpaRepository.findByMovieIdOrderByCreatedAtDesc(movieId)
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Review> findByUserId(String userId) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsByBookingId(String bookingId) {
        return jpaRepository.existsByBookingId(bookingId);
    }

    // ── Mapping helpers ──────────────────────────────────────────────────────

    private ReviewJpaEntity toJpa(Review r) {
        ReviewJpaEntity e = new ReviewJpaEntity();
        e.setId(r.getId());
        e.setBookingId(r.getBookingId());
        e.setUserId(r.getUserId());
        e.setMovieId(r.getMovieId());
        e.setRating(r.getRating());
        e.setComment(r.getComment());
        return e;
    }

    private Review toDomain(ReviewJpaEntity e) {
        return new Review(
                e.getId(), e.getBookingId(), e.getUserId(),
                e.getMovieId(), e.getRating(), e.getComment(),
                e.getCreatedAt(), e.getUpdatedAt()
        );
    }
}
