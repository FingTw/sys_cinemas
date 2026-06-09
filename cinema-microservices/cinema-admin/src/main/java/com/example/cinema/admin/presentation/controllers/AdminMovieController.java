package com.example.cinema.admin.presentation.controllers;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.cinema.admin.application.ports.in.AdminMovieUseCase;
import com.example.cinema.admin.application.dto.MovieDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/admin/movies")
@RequiredArgsConstructor
@Slf4j
public class AdminMovieController {

    private final AdminMovieUseCase adminMovieUseCase;

    @PreAuthorize("hasAuthority('MOVIE_CREATE')")
    @PostMapping
    public ResponseEntity<MovieDTO> createMovie(@RequestBody MovieDTO request) {
        log.info("Admin creating new movie: [{}]", request.getTitle());
        return ResponseEntity.ok(adminMovieUseCase.createMovie(request));
    }

    @PreAuthorize("hasAuthority('MOVIE_READ')")
    @GetMapping
    public ResponseEntity<List<MovieDTO>> getAllMovies() {
        log.info("Fetching all movies for admin");
        return ResponseEntity.ok(adminMovieUseCase.getAllMovies());
    }

    @PreAuthorize("hasAuthority('MOVIE_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<MovieDTO> updateMovie(@PathVariable String id, @RequestBody MovieDTO request) {
        log.info("Admin updating movie ID: [{}]", id);
        return ResponseEntity.ok(adminMovieUseCase.updateMovie(id, request));
    }

    @PreAuthorize("hasAuthority('MOVIE_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMovie(@PathVariable String id) {
        log.info("Admin deleting movie ID: [{}]", id);
        adminMovieUseCase.deleteMovie(id);
        return ResponseEntity.ok(Map.of("message", "Movie deleted successfully"));
    }
}
