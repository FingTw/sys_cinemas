package com.example.cinema.admin.presentation.controllers;

import com.example.cinema.admin.application.dto.FeaturedMovieDTO;
import com.example.cinema.admin.application.dto.MovieDTO;
import com.example.cinema.admin.application.dto.GenreDTO;
import com.example.cinema.admin.infrastructure.database.entities.FeaturedMovieJpaEntity;
import com.example.cinema.admin.infrastructure.database.entities.MovieJpaEntity;
import com.example.cinema.admin.infrastructure.database.entities.GenreJpaEntity;
import com.example.cinema.admin.infrastructure.database.repositories.SpringDataFeaturedMovieRepository;
import com.example.cinema.admin.infrastructure.database.repositories.SpringDataMovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/featured-movies")
@RequiredArgsConstructor
@Slf4j
public class AdminFeaturedMovieController {

    private final SpringDataFeaturedMovieRepository featuredMovieRepository;
    private final SpringDataMovieRepository movieRepository;
    private final StringRedisTemplate redisTemplate;
    
    private void clearCache() {
        redisTemplate.delete("cache:home-overview");
        log.info("[CINEMA-ADMIN] Cleared cache:home-overview in Redis");
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MOVIE_READ')")
    public ResponseEntity<List<FeaturedMovieDTO>> getAllFeaturedMovies() {
        log.info("[CINEMA-ADMIN] Fetching all featured movies");
        List<FeaturedMovieJpaEntity> entities = featuredMovieRepository.findAllByOrderByDisplayOrderAsc();
        
        List<FeaturedMovieDTO> dtos = entities.stream().map(this::mapToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{movieId}")
    @PreAuthorize("hasAuthority('MOVIE_UPDATE')")
    public ResponseEntity<?> addFeaturedMovie(@PathVariable String movieId) {
        log.info("[CINEMA-ADMIN] Adding featured movie: {}", movieId);
        
        if (featuredMovieRepository.existsByMovieId(movieId)) {
            return ResponseEntity.badRequest().body("Movie is already featured");
        }
        
        MovieJpaEntity movie = movieRepository.findById(movieId).orElse(null);
        if (movie == null) {
            return ResponseEntity.badRequest().body("Movie not found");
        }
        
        // Find max display_order
        List<FeaturedMovieJpaEntity> all = featuredMovieRepository.findAllByOrderByDisplayOrderAsc();
        int nextOrder = all.isEmpty() ? 1 : all.get(all.size() - 1).getDisplayOrder() + 1;
        
        FeaturedMovieJpaEntity newFeatured = FeaturedMovieJpaEntity.builder()
            .movie(movie)
            .displayOrder(nextOrder)
            .createdAt(LocalDateTime.now())
            .build();
            
        featuredMovieRepository.save(newFeatured);
        clearCache();
        return ResponseEntity.ok(mapToDTO(newFeatured));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MOVIE_UPDATE')")
    public ResponseEntity<?> removeFeaturedMovie(@PathVariable String id) {
        log.info("[CINEMA-ADMIN] Removing featured movie: {}", id);
        featuredMovieRepository.deleteById(id);
        clearCache();
        return ResponseEntity.ok().build();
    }

    @PutMapping("/reorder")
    @PreAuthorize("hasAuthority('MOVIE_UPDATE')")
    public ResponseEntity<?> reorderFeaturedMovies(@RequestBody List<String> orderedIds) {
        log.info("[CINEMA-ADMIN] Reordering featured movies: {}", orderedIds);
        
        List<FeaturedMovieJpaEntity> entities = featuredMovieRepository.findAll();
        for (FeaturedMovieJpaEntity entity : entities) {
            int index = orderedIds.indexOf(entity.getId());
            if (index != -1) {
                entity.setDisplayOrder(index + 1);
                featuredMovieRepository.save(entity);
            }
        }
        clearCache();
        return ResponseEntity.ok().build();
    }
    
    private FeaturedMovieDTO mapToDTO(FeaturedMovieJpaEntity entity) {
        MovieJpaEntity m = entity.getMovie();
        
        List<GenreDTO> genreDTOs = m.getGenres() != null ? m.getGenres().stream()
            .map(g -> GenreDTO.builder()
                .id(g.getId())
                .name(g.getName())
                .code(g.getCode())
                .build())
            .collect(Collectors.toList()) : null;

        List<String> genreIds = m.getGenres() != null ? m.getGenres().stream()
            .map(GenreJpaEntity::getId)
            .collect(Collectors.toList()) : null;

        MovieDTO movieDTO = MovieDTO.builder()
            .id(m.getId())
            .title(m.getTitle())
            .description(m.getDescription())
            .durationMinutes(m.getDurationMinutes())
            .releaseDate(m.getReleaseDate())
            .posterUrl(m.getPosterUrl())
            .genres(genreDTOs)
            .genreIds(genreIds)
            .status(m.getStatus())
            .build();
            
        return FeaturedMovieDTO.builder()
            .id(entity.getId())
            .displayOrder(entity.getDisplayOrder())
            .createdAt(entity.getCreatedAt())
            .movie(movieDTO)
            .build();
    }
}
