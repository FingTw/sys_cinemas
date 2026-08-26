package com.example.cinema.admin.controllers;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.example.cinema.admin.services.AdminGenreUseCaseImpl;
import com.example.cinema.admin.dto.GenreDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/admin/genres")
@RequiredArgsConstructor
@Slf4j
public class AdminGenreController {

    private final AdminGenreUseCaseImpl adminGenreUseCase;
    private final StringRedisTemplate redisTemplate;

    private void clearGenreCache() {
        try {
            // 1. Xóa cache Gateway aggregation BFF
            redisTemplate.delete("cache:home-overview");
            log.info("[CINEMA-ADMIN] Cleared cache:home-overview due to genre changes");

            // 2. Xóa các cache Thể loại (genres::*)
            java.util.Set<String> genresKeys = redisTemplate.keys("genres::*");
            if (genresKeys != null && !genresKeys.isEmpty()) {
                redisTemplate.delete(genresKeys);
                log.info("[CINEMA-ADMIN] Evicted genres cache keys: {}", genresKeys);
            }

            // 3. Xóa cache chi tiết (genre::*)
            java.util.Set<String> genreKeys = redisTemplate.keys("genre::*");
            if (genreKeys != null && !genreKeys.isEmpty()) {
                redisTemplate.delete(genreKeys);
                log.info("[CINEMA-ADMIN] Evicted genre detail cache keys: {}", genreKeys);
            }
        } catch (Exception e) {
            log.warn("[CINEMA-ADMIN] Failed to clear genre caches: {}", e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('MOVIE_CREATE')")
    @PostMapping
    public ResponseEntity<GenreDTO> createGenre(@RequestBody GenreDTO request) {
        log.info("Admin creating new genre: [{}]", request.getName());
        GenreDTO response = adminGenreUseCase.createGenre(request);
        clearGenreCache();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('MOVIE_READ')")
    @GetMapping
    public ResponseEntity<List<GenreDTO>> getAllGenres() {
        log.info("Fetching all genres for admin");
        return ResponseEntity.ok(adminGenreUseCase.getAllGenres());
    }

    @PreAuthorize("hasAuthority('MOVIE_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<GenreDTO> getGenreById(@PathVariable String id) {
        log.info("Fetching genre ID: [{}]", id);
        return ResponseEntity.ok(adminGenreUseCase.getGenreById(id));
    }

    @PreAuthorize("hasAuthority('MOVIE_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<GenreDTO> updateGenre(@PathVariable String id, @RequestBody GenreDTO request) {
        log.info("Admin updating genre ID: [{}]", id);
        GenreDTO response = adminGenreUseCase.updateGenre(id, request);
        clearGenreCache();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('MOVIE_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGenre(@PathVariable String id) {
        log.info("Admin deleting genre ID: [{}]", id);
        adminGenreUseCase.deleteGenre(id);
        clearGenreCache();
        return ResponseEntity.ok(Map.of("message", "Genre deleted successfully"));
    }
}
