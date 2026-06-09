package com.example.cinema.catalog.presentation.controllers;

import com.example.cinema.catalog.application.dto.MovieDTO;
import com.example.cinema.catalog.application.ports.in.MovieService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Public API - Khong yeu cau xac thuc.
 * Cho phep trang chu hien thi danh sach phim.
 */
@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
@Slf4j
public class PublicMovieController {

    private final MovieService movieService;

    @GetMapping
    public ResponseEntity<List<MovieDTO>> getShowingMovies() {
        log.info("[Public] Fetching showing movies for home page");
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    @GetMapping("/stats/count")
    public ResponseEntity<Long> getMovieCount() {
        return ResponseEntity.ok((long) movieService.getAllMovies().size());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieDTO> getMovieById(@PathVariable String id) {
        log.info("[Public] Viewing details for movie ID: [{}]", id);
        return ResponseEntity.ok(movieService.getMovieById(id));
    }
}
