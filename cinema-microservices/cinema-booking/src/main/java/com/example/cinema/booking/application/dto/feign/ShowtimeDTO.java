package com.example.cinema.booking.application.dto.feign;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowtimeDTO {
    private String id;
    private String movieId;
    private String movieTitle;
    private Integer movieDuration;
    private String roomId;
    private String roomName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal price;
    private BigDecimal priceVip;
    private BigDecimal priceCouple;
}
