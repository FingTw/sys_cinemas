package com.example.cinema.catalog.presentation.controllers;

import com.example.cinema.catalog.application.dto.FeaturedMovieDTO;
import com.example.cinema.catalog.application.dto.MovieDTO;
import com.example.cinema.catalog.application.dto.GenreDTO;
import com.example.cinema.catalog.infrastructure.database.entities.FeaturedMovieJpaEntity;
import com.example.cinema.catalog.infrastructure.database.entities.MovieJpaEntity;
import com.example.cinema.catalog.infrastructure.database.entities.GenreJpaEntity;
import com.example.cinema.catalog.infrastructure.database.repositories.SpringDataFeaturedMovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/featured-movies")
@RequiredArgsConstructor
@Slf4j
public class PublicFeaturedMovieController {

    private final SpringDataFeaturedMovieRepository featuredMovieRepository;

    @GetMapping
    public ResponseEntity<List<FeaturedMovieDTO>> getFeaturedMovies() {
        log.info("[Public] Fetching featured movies sorted by displayOrder");
        List<FeaturedMovieJpaEntity> entities = featuredMovieRepository.findAllByOrderByDisplayOrderAsc();
        
        List<FeaturedMovieDTO> dtos = entities.stream().map(entity -> {
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
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
}
