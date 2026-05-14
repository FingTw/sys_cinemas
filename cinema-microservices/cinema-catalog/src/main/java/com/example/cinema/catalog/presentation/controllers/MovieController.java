package com.example.cinema.catalog.presentation.controllers;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.cinema.catalog.application.dto.MovieDTO;
import com.example.cinema.catalog.application.usecases.MovieUseCase;

@RestController
@RequestMapping("/api/v1/admin/movies")
public class MovieController {

    private static final Logger log = LoggerFactory.getLogger(MovieController.class);

    private final MovieUseCase movieUseCase;

    public MovieController(MovieUseCase movieUseCase) {
        this.movieUseCase = movieUseCase;
    }

    @PreAuthorize("hasAuthority('MOVIE_CREATE')")
    @PostMapping
    public ResponseEntity<MovieDTO> createMovie(@RequestBody MovieDTO request) {
        log.info("Admin creating new movie: [{}]", request.getTitle());
        try {
            return ResponseEntity.ok(movieUseCase.createMovie(request));
        } catch (Exception e) {
            log.error("Failed to create movie [{}]: {}", request.getTitle(), e.getMessage());
            throw e;
        }
    }

    @PreAuthorize("hasAuthority('MOVIE_READ')")
    @GetMapping
    public ResponseEntity<List<MovieDTO>> getAllMovies() {
        log.info("Fetching all movies");
        return ResponseEntity.ok(movieUseCase.getAllMovies());
    }

    @PreAuthorize("hasAuthority('MOVIE_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<MovieDTO> updateMovie(@PathVariable String id, @RequestBody MovieDTO request) {
        log.info("Admin updating movie ID: [{}]", id);
        try {
            return ResponseEntity.ok(movieUseCase.updateMovie(id, request));
        } catch (Exception e) {
            log.error("Failed to update movie [{}]: {}", id, e.getMessage());
            throw e;
        }
    }

    @PreAuthorize("hasAuthority('MOVIE_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMovie(@PathVariable String id) {
        log.info("Admin deleting movie ID: [{}]", id);
        try {
            movieUseCase.deleteMovie(id);
            log.info("Successfully deleted movie [{}]", id);
            return ResponseEntity.ok(Map.of("message", "Movie deleted successfully"));
        } catch (Exception e) {
            log.error("Failed to delete movie [{}]: {}", id, e.getMessage());
            throw e;
        }
    }
}
