package com.example.cinema.admin.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeaturedMovieDTO {
    private String id;
    private MovieDTO movie;
    private Integer displayOrder;
    private LocalDateTime createdAt;
}
