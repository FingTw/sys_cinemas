package com.example.cinema.catalog.infrastructure.database.adapters;

import com.example.cinema.catalog.domain.entities.Movie;
import com.example.cinema.catalog.domain.entities.Genre;
import com.example.cinema.catalog.domain.repositories.MovieRepository;
import com.example.cinema.catalog.infrastructure.database.entities.MovieJpaEntity;
import com.example.cinema.catalog.infrastructure.database.entities.GenreJpaEntity;
import com.example.cinema.catalog.infrastructure.database.repositories.SpringDataMovieRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MovieRepositoryAdapter implements MovieRepository {

    private final SpringDataMovieRepository springDataMovieRepository;

    public MovieRepositoryAdapter(SpringDataMovieRepository springDataMovieRepository) {
        this.springDataMovieRepository = springDataMovieRepository;
    }

    @Override
    public Movie save(Movie movie) {
        MovieJpaEntity entity = toEntity(movie);
        MovieJpaEntity saved = springDataMovieRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Movie> findById(String id) {
        return springDataMovieRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Movie> findAll() {
        return springDataMovieRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        springDataMovieRepository.deleteById(id);
    }

    private Movie toDomain(MovieJpaEntity entity) {
        java.util.Set<Genre> genres = entity.getGenres() != null ? entity.getGenres().stream()
                .map(g -> Genre.builder().id(g.getId()).name(g.getName()).code(g.getCode()).build())
                .collect(Collectors.toSet()) : new java.util.HashSet<>();
        return Movie.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .durationMinutes(entity.getDurationMinutes())
                .releaseDate(entity.getReleaseDate())
                .posterUrl(entity.getPosterUrl())
                .genres(genres)
                .status(entity.getStatus())
                .build();
    }

    private MovieJpaEntity toEntity(Movie domain) {
        java.util.Set<GenreJpaEntity> genres = domain.getGenres() != null ? domain.getGenres().stream()
                .map(g -> GenreJpaEntity.builder().id(g.getId()).name(g.getName()).code(g.getCode()).build())
                .collect(Collectors.toSet()) : new java.util.HashSet<>();
        return MovieJpaEntity.builder()
                .id(domain.getId())
                .title(domain.getTitle())
                .description(domain.getDescription())
                .durationMinutes(domain.getDurationMinutes())
                .releaseDate(domain.getReleaseDate())
                .posterUrl(domain.getPosterUrl())
                .genres(genres)
                .status(domain.getStatus())
                .build();
    }
}
