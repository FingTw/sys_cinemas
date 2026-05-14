package com.example.cinema.catalog.infrastructure.database.entities;

import jakarta.persistence.*;
import java.time.LocalDate;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "movies", schema = "catalog")
@SQLDelete(sql = "UPDATE catalog.movies SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
public class MovieJpaEntity {
    @jakarta.persistence.Column(name = "is_deleted")
    private boolean isDeleted = false;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer durationMinutes;

    private LocalDate releaseDate;
    
    private String posterUrl;
    private String genre;
    private String status;

    public MovieJpaEntity() {
    }

    public MovieJpaEntity(String id, String title, String description, Integer durationMinutes, LocalDate releaseDate, String posterUrl, String genre, String status) {
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
    public static MovieJpaEntityBuilder builder() {
        return new MovieJpaEntityBuilder();
    }

    public static class MovieJpaEntityBuilder {
        private String id;
        private String title;
        private String description;
        private Integer durationMinutes;
        private LocalDate releaseDate;
        private String posterUrl;
        private String genre;
        private String status;

        public MovieJpaEntityBuilder id(String id) {
            this.id = id;
            return this;
        }

        public MovieJpaEntityBuilder title(String title) {
            this.title = title;
            return this;
        }

        public MovieJpaEntityBuilder description(String description) {
            this.description = description;
            return this;
        }

        public MovieJpaEntityBuilder durationMinutes(Integer durationMinutes) {
            this.durationMinutes = durationMinutes;
            return this;
        }

        public MovieJpaEntityBuilder releaseDate(LocalDate releaseDate) {
            this.releaseDate = releaseDate;
            return this;
        }

        public MovieJpaEntityBuilder posterUrl(String posterUrl) {
            this.posterUrl = posterUrl;
            return this;
        }

        public MovieJpaEntityBuilder genre(String genre) {
            this.genre = genre;
            return this;
        }

        public MovieJpaEntityBuilder status(String status) {
            this.status = status;
            return this;
        }

        public MovieJpaEntity build() {
            return new MovieJpaEntity(id, title, description, durationMinutes, releaseDate, posterUrl, genre, status);
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
