package com.example.cinema.scheduling.application.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowtimeDTO {
    private String id;
    private String movieId;
    private String movieTitle;   // For display purposes
    private Integer movieDuration;
    private String roomId;
    private String roomName;     // For display purposes
    private String cinemaId;
    private String cinemaName;
    private String cinemaComplexName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private BigDecimal price;
    private BigDecimal priceVip;
    private BigDecimal priceCouple;
}
