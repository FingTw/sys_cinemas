package com.example.cinema.admin.domain.entities;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
public class Showtime {
    private String id;
    private String movieId;
    private String roomId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status; // SCHEDULED, PLAYING, COMPLETED, CANCELLED
    private BigDecimal price;
    private BigDecimal priceVip;
    private BigDecimal priceCouple;

    @Builder
    public Showtime(String id, String movieId, String roomId, LocalDateTime startTime,
                    LocalDateTime endTime, String status, BigDecimal price,
                    BigDecimal priceVip, BigDecimal priceCouple) {
        this.id = (id != null && !id.trim().isEmpty()) ? id : java.util.UUID.randomUUID().toString();
        this.movieId = movieId;
        this.roomId = roomId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.price = price;
        this.priceVip = priceVip;
        this.priceCouple = priceCouple;
    }

    public void updateDetails(LocalDateTime startTime, LocalDateTime endTime, String status, BigDecimal price, BigDecimal priceVip, BigDecimal priceCouple) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.price = price;
        this.priceVip = priceVip;
        this.priceCouple = priceCouple;
    }
}
