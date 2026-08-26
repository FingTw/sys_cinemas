package com.example.cinema.admin.services;

import com.example.cinema.admin.dto.MovieDTO;
import com.example.cinema.admin.dto.GenreDTO;
import com.example.cinema.admin.services.AdminMovieUseCase;
import com.example.cinema.admin.entities.Movie;
import com.example.cinema.admin.entities.Genre;
import com.example.cinema.admin.repositories.MovieRepository;
import com.example.cinema.admin.repositories.GenreRepository;
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
import com.example.cinema.admin.utils.SecurityUtils;
import com.example.cinema.admin.repositories.ShowtimeRepository;
import com.example.cinema.admin.repositories.RoomRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminMovieUseCaseImpl implements AdminMovieUseCase {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final ModelMapper modelMapper;
    private final ShowtimeRepository showtimeRepository;
    private final RoomRepository roomRepository;

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
            java.util.Set<Genre> genres = new java.util.HashSet<>();
            if (dto.getGenreIds() != null && !dto.getGenreIds().isEmpty()) {
                genres = new java.util.HashSet<>(genreRepository.findAllById(dto.getGenreIds()));
            }
            Movie movie = Movie.builder()
                    .title(dto.getTitle())
                    .description(dto.getDescription())
                    .durationMinutes(dto.getDurationMinutes())
                    .releaseDate(dto.getReleaseDate())
                    .posterUrl(dto.getPosterUrl())
                    .genres(genres)
                    .status(status)
                    .build();
            Movie saved = movieRepository.save(movie);
            log.info("Admin created new Movie in Database: [{}] - ID: [{}]", saved.getTitle(), saved.getId());
            return convertToDTO(saved);
        } catch (Exception e) {
            throw new ServerException("Movie creation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<MovieDTO> getAllMovies() {
        try {
            List<Movie> movies = movieRepository.findAll();
            String staffCinemaId = SecurityUtils.getStaffCinemaId();
            
            if (staffCinemaId != null && !staffCinemaId.trim().isEmpty()) {
                java.util.List<String> roomIds = roomRepository.findAll().stream()
                        .filter(r -> staffCinemaId.equals(r.getCinemaId()))
                        .map(r -> r.getId())
                        .collect(Collectors.toList());
                
                java.util.Set<String> movieIds = showtimeRepository.findAll().stream()
                        .filter(s -> roomIds.contains(s.getRoomId()))
                        .map(s -> s.getMovieId())
                        .collect(Collectors.toSet());
                        
                movies = movies.stream()
                        .filter(m -> movieIds.contains(m.getId()))
                        .collect(Collectors.toList());
            }

            return movies.stream()
                    .map(this::convertToDTO)
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

            java.util.Set<Genre> genres = new java.util.HashSet<>();
            if (dto.getGenreIds() != null && !dto.getGenreIds().isEmpty()) {
                genres = new java.util.HashSet<>(genreRepository.findAllById(dto.getGenreIds()));
            }

            movie.updateDetails(
                    dto.getTitle(),
                    dto.getDescription(),
                    dto.getDurationMinutes(),
                    dto.getReleaseDate(),
                    dto.getPosterUrl(),
                    genres,
                    dto.getStatus()
            );

            Movie updated = movieRepository.save(movie);
            log.info("Successfully updated Movie ID: [{}]", updated.getId());
            return convertToDTO(updated);
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

    private MovieDTO convertToDTO(Movie movie) {
        MovieDTO dto = modelMapper.map(movie, MovieDTO.class);
        if (movie.getGenres() != null) {
            dto.setGenres(movie.getGenres().stream()
                    .map(g -> modelMapper.map(g, GenreDTO.class))
                    .collect(Collectors.toList()));
            dto.setGenreIds(movie.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toList()));
        }
        return dto;
    }
}
