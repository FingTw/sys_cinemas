package com.example.cinema.booking.application.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {
    private String id;
    private String bookingId;
    private String userId;
    private String username;
    private String movieId;
    private String movieTitle;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
}
