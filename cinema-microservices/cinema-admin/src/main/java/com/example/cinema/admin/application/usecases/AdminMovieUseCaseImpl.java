package com.example.cinema.admin.application.usecases;

import com.example.cinema.admin.application.dto.MovieDTO;
import com.example.cinema.admin.application.ports.in.AdminMovieUseCase;
import com.example.cinema.admin.domain.entities.Movie;
import com.example.cinema.admin.domain.repositories.MovieRepository;
import com.example.cinema.common.exception.ServerException;
import com.example.cinema.common.exception.ClientException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.modelmapper.ModelMapper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminMovieUseCaseImpl implements AdminMovieUseCase {

    private final MovieRepository movieRepository;
    private final ModelMapper modelMapper;

    @Override
    @Caching(evict = {
            @CacheEvict(value = "movies", allEntries = true),
            @CacheEvict(value = "movieCount", allEntries = true)
    })
    public MovieDTO createMovie(MovieDTO dto) {
        try {
            String status = dto.getStatus();
            if (status == null) {
                status = "COMING_SOON";
            }
            Movie movie = Movie.builder()
                    .title(dto.getTitle())
                    .description(dto.getDescription())
                    .durationMinutes(dto.getDurationMinutes())
                    .releaseDate(dto.getReleaseDate())
                    .posterUrl(dto.getPosterUrl())
                    .genre(dto.getGenre())
                    .status(status)
                    .build();
            Movie saved = movieRepository.save(movie);
            log.info("Admin created new Movie in Database: [{}] - ID: [{}]", saved.getTitle(), saved.getId());
            return modelMapper.map(saved, MovieDTO.class);
        } catch (Exception e) {
            throw new ServerException("Movie creation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<MovieDTO> getAllMovies() {
        try {
            List<Movie> movies = movieRepository.findAll();
            return movies.stream()
                    .map(movie -> modelMapper.map(movie, MovieDTO.class))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ServerException("Failed to retrieve movies: " + e.getMessage(), e);
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "movies", allEntries = true),
            @CacheEvict(value = "movie", key = "#id"),
            @CacheEvict(value = "movieCount", allEntries = true)
    })
    public MovieDTO updateMovie(String id, MovieDTO dto) {
        try {
            Movie movie = movieRepository.findById(id)
                    .orElseThrow(() -> new ClientException("Movie not found with ID: " + id));

            movie.updateDetails(
                    dto.getTitle(),
                    dto.getDescription(),
                    dto.getDurationMinutes(),
                    dto.getReleaseDate(),
                    dto.getPosterUrl(),
                    dto.getGenre(),
                    dto.getStatus()
            );

            Movie updated = movieRepository.save(movie);
            log.info("Successfully updated Movie ID: [{}]", updated.getId());
            return modelMapper.map(updated, MovieDTO.class);
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            throw new ServerException("Movie update failed: " + e.getMessage(), e);
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "movies", allEntries = true),
            @CacheEvict(value = "movie", key = "#id"),
            @CacheEvict(value = "movieCount", allEntries = true)
    })
    public void deleteMovie(String id) {
        log.info("Deleting Movie ID: [{}] from Database...", id);
        try {
            movieRepository.deleteById(id);
            log.info("Successfully deleted movie!");
        } catch (Exception e) {
            throw new ServerException("Movie deletion failed: " + e.getMessage(), e);
        }
    }
}
