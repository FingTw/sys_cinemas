package com.example.cinema.domain.entities;

import java.time.LocalDate;

public class Movie {
    private String id;
    private String title;
    private String description;
    private Integer durationMinutes;
    private LocalDate releaseDate;
    private String posterUrl;
    private String genre;
    private String status; // COMING_SOON, SHOWING, STOPPED

    public Movie() {
    }

    public Movie(String id, String title, String description, Integer durationMinutes, LocalDate releaseDate, String posterUrl, String genre, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.releaseDate = releaseDate;
        this.posterUrl = posterUrl;
        this.genre = genre;
        this.status = status;
    }

    // Builder manual
    public static MovieBuilder builder() {
        return new MovieBuilder();
    }

    public static class MovieBuilder {
        private String id;
        private String title;
        private String description;
        private Integer durationMinutes;
        private LocalDate releaseDate;
        private String posterUrl;
        private String genre;
        private String status;

        public MovieBuilder id(String id) {
            this.id = id;
            return this;
        }

        public MovieBuilder title(String title) {
            this.title = title;
            return this;
        }

        public MovieBuilder description(String description) {
            this.description = description;
            return this;
        }

        public MovieBuilder durationMinutes(Integer durationMinutes) {
            this.durationMinutes = durationMinutes;
            return this;
        }

        public MovieBuilder releaseDate(LocalDate releaseDate) {
            this.releaseDate = releaseDate;
            return this;
        }

        public MovieBuilder posterUrl(String posterUrl) {
            this.posterUrl = posterUrl;
            return this;
        }

        public MovieBuilder genre(String genre) {
            this.genre = genre;
            return this;
        }

        public MovieBuilder status(String status) {
            this.status = status;
            return this;
        }

        public Movie build() {
            return new Movie(id, title, description, durationMinutes, releaseDate, posterUrl, genre, status);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
