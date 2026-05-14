package com.example.cinema.infrastructure.database.repositories;

import com.example.cinema.domain.entities.Movie;
import com.example.cinema.domain.repositories.MovieRepository;
import com.example.cinema.infrastructure.database.entities.MovieJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MovieRepositoryImpl implements MovieRepository {
    private final SpringDataMovieRepository springDataRepo;

    public MovieRepositoryImpl(SpringDataMovieRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public Movie save(Movie movie) {
        MovieJpaEntity jpaEntity = mapToJpa(movie);
        MovieJpaEntity saved = springDataRepo.save(jpaEntity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<Movie> findById(String id) {
        return springDataRepo.findById(id).map(this::mapToDomain);
    }

    @Override
    public List<Movie> findAll() {
        return springDataRepo.findAll().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        springDataRepo.deleteById(id);
    }

    private MovieJpaEntity mapToJpa(Movie movie) {
        return MovieJpaEntity.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .description(movie.getDescription())
                .durationMinutes(movie.getDurationMinutes())
                .releaseDate(movie.getReleaseDate())
                .posterUrl(movie.getPosterUrl())
                .genre(movie.getGenre())
                .status(movie.getStatus())
                .build();
    }

    private Movie mapToDomain(MovieJpaEntity jpa) {
        return Movie.builder()
                .id(jpa.getId())
                .title(jpa.getTitle())
                .description(jpa.getDescription())
                .durationMinutes(jpa.getDurationMinutes())
                .releaseDate(jpa.getReleaseDate())
                .posterUrl(jpa.getPosterUrl())
                .genre(jpa.getGenre())
                .status(jpa.getStatus())
                .build();
    }
}
