package com.example.cinema.admin.infrastructure.database.adapters;

import com.example.cinema.admin.domain.entities.Movie;
import com.example.cinema.admin.domain.repositories.MovieRepository;
import com.example.cinema.admin.infrastructure.database.entities.MovieJpaEntity;
import com.example.cinema.admin.infrastructure.database.repositories.SpringDataMovieRepository;
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
        return Movie.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .durationMinutes(entity.getDurationMinutes())
                .releaseDate(entity.getReleaseDate())
                .posterUrl(entity.getPosterUrl())
                .genre(entity.getGenre())
                .status(entity.getStatus())
                .build();
    }

    private MovieJpaEntity toEntity(Movie domain) {
        return MovieJpaEntity.builder()
                .id(domain.getId())
                .title(domain.getTitle())
                .description(domain.getDescription())
                .durationMinutes(domain.getDurationMinutes())
                .releaseDate(domain.getReleaseDate())
                .posterUrl(domain.getPosterUrl())
                .genre(domain.getGenre())
                .status(domain.getStatus())
                .build();
    }
}
