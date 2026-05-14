package com.example.cinema.presentation.controllers;

import com.example.cinema.application.dto.MovieDTO;
import com.example.cinema.application.usecases.MovieUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public API - Khong yeu cau xac thuc.
 * Cho phep trang chu hien thi danh sach phim.
 */
@RestController
@RequestMapping("/api/v1/movies")
public class PublicMovieController {

    private static final Logger log = LoggerFactory.getLogger(PublicMovieController.class);

    private final MovieUseCase movieUseCase;

    public PublicMovieController(MovieUseCase movieUseCase) {
        this.movieUseCase = movieUseCase;
    }

    @GetMapping
    public ResponseEntity<List<MovieDTO>> getShowingMovies() {
        log.info("[Public] Fetching showing movies for home page");
        return ResponseEntity.ok(movieUseCase.getAllMovies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieDTO> getMovieById(@PathVariable String id) {
        log.info("[Public] Viewing details for movie ID: [{}]", id);
        return ResponseEntity.ok(movieUseCase.getMovieById(id));
    }
}
