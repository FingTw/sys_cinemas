package com.example.cinema.admin.controllers;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.example.cinema.admin.services.AdminMovieUseCaseImpl;
import com.example.cinema.admin.dto.MovieDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/admin/movies")
@RequiredArgsConstructor
@Slf4j
public class AdminMovieController {

    private final AdminMovieUseCaseImpl adminMovieUseCase;
    private final StringRedisTemplate redisTemplate;

    private void clearHomeCache() {
        try {
            // 1. Xóa cache Gateway aggregation BFF
            redisTemplate.delete("cache:home-overview");
            log.info("[CINEMA-ADMIN] Cleared cache:home-overview due to movie changes");

            // 2. Xóa cache danh sách phim (movies::*) của Catalog Service
            java.util.Set<String> moviesKeys = redisTemplate.keys("movies::*");
            if (moviesKeys != null && !moviesKeys.isEmpty()) {
                redisTemplate.delete(moviesKeys);
                log.info("[CINEMA-ADMIN] Evicted movies cache keys: {}", moviesKeys);
            }

            // 3. Xóa cache chi tiết phim (movie::*) của Catalog Service
            java.util.Set<String> movieKeys = redisTemplate.keys("movie::*");
            if (movieKeys != null && !movieKeys.isEmpty()) {
                redisTemplate.delete(movieKeys);
                log.info("[CINEMA-ADMIN] Evicted movie detail cache keys: {}", movieKeys);
            }

            // 4. Xóa cache số lượng phim (movieCount::*) của Catalog Service
            java.util.Set<String> countKeys = redisTemplate.keys("movieCount::*");
            if (countKeys != null && !countKeys.isEmpty()) {
                redisTemplate.delete(countKeys);
                log.info("[CINEMA-ADMIN] Evicted movie count cache keys: {}", countKeys);
            }
        } catch (Exception e) {
            log.warn("[CINEMA-ADMIN] Failed to clear movie caches: {}", e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('MOVIE_CREATE')")
    @PostMapping
    public ResponseEntity<MovieDTO> createMovie(@RequestBody MovieDTO request) {
        log.info("Admin creating new movie: [{}]", request.getTitle());
        MovieDTO response = adminMovieUseCase.createMovie(request);
        clearHomeCache();
        return ResponseEntity.ok(response);
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
        MovieDTO response = adminMovieUseCase.updateMovie(id, request);
        clearHomeCache();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('MOVIE_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMovie(@PathVariable String id) {
        log.info("Admin deleting movie ID: [{}]", id);
        adminMovieUseCase.deleteMovie(id);
        clearHomeCache();
        return ResponseEntity.ok(Map.of("message", "Movie deleted successfully"));
    }
}
