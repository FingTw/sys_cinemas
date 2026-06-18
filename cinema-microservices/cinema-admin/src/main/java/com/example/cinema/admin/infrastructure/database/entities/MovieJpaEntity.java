package com.example.cinema.admin.infrastructure.database.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "movies", schema = "catalog")
@SQLDelete(sql = "UPDATE catalog.movies SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieJpaEntity {
    @Builder.Default
    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @Id
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer durationMinutes;

    private LocalDate releaseDate;
    private String posterUrl;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "movie_genres",
        schema = "catalog",
        joinColumns = @JoinColumn(name = "movie_id"),
        inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @Builder.Default
    private java.util.Set<GenreJpaEntity> genres = new java.util.HashSet<>();

    private String status;
}
