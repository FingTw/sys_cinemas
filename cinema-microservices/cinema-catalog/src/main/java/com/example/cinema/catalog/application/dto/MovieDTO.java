package com.example.cinema.catalog.application.dto;

import java.time.LocalDate;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieDTO {
    private String id;
    private String title;
    private String description;
    private Integer durationMinutes;
    private LocalDate releaseDate;
    private String posterUrl;
    private java.util.List<String> genreIds;
    private java.util.List<GenreDTO> genres;
    private String status;
}
