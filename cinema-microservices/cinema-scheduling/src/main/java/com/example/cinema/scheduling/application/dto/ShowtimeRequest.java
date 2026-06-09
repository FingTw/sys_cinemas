package com.example.cinema.scheduling.application.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowtimeRequest {
    private String movieId;
    private String roomId;
    private LocalDateTime startTime;
    private BigDecimal price;
    private BigDecimal priceVip;
    private BigDecimal priceCouple;
}
