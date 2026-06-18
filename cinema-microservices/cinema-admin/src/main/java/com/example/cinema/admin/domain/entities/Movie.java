package com.example.cinema.admin.domain.entities;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
public class Movie {
    private String id;
    private String title;
    private String description;
    private Integer durationMinutes;
    private LocalDate releaseDate;
    private String posterUrl;
    private java.util.Set<Genre> genres = new java.util.HashSet<>();
    private String status;

    @Builder
    public Movie(String id, String title, String description, Integer durationMinutes, LocalDate releaseDate, String posterUrl, java.util.Set<Genre> genres, String status) {
        this.id = (id != null && !id.trim().isEmpty()) ? id : java.util.UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.releaseDate = releaseDate;
        this.posterUrl = posterUrl;
        this.genres = genres != null ? genres : new java.util.HashSet<>();
        this.status = status;
    }

    public void updateDetails(String title, String description, Integer durationMinutes, LocalDate releaseDate, String posterUrl, java.util.Set<Genre> genres, String status) {
        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.releaseDate = releaseDate;
        this.posterUrl = posterUrl;
        this.genres = genres != null ? genres : new java.util.HashSet<>();
        this.status = status;
    }

    public void updateStatus(String status) {
        this.status = status;
    }
}
