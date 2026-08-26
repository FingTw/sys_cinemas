package com.example.cinema.booking.domain;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Review {
    private String id;
    private String bookingId;
    private String userId;
    private String movieId;
    private int rating; // 1–5
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Review() {}

    public static Review create(String bookingId, String userId, String movieId, int rating, String comment) {
        Review r = new Review();
        r.setBookingId(bookingId);
        r.setUserId(userId);
        r.setMovieId(movieId);
        r.setRating(rating);
        r.setComment(comment);
        r.setCreatedAt(LocalDateTime.now());
        r.setUpdatedAt(LocalDateTime.now());
        return r;
    }

    public Review(String id, String bookingId, String userId, String movieId, int rating, String comment,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.userId = userId;
        this.movieId = movieId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
