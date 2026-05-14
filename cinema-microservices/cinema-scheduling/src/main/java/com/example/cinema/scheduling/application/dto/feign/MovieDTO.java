package com.example.cinema.scheduling.application.dto.feign;

import java.time.LocalDate;

public class MovieDTO {
    private String id;
    private String title;
    private String description;
    private Integer durationMinutes;
    private LocalDate releaseDate;
    private String posterUrl;
    private String genre;
    private String status;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }
    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
