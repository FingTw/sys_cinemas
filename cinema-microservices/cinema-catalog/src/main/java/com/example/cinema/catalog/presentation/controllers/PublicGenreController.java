package com.example.cinema.catalog.presentation.controllers;

import com.example.cinema.catalog.application.dto.GenreDTO;
import com.example.cinema.catalog.domain.repositories.GenreRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.cache.annotation.Cacheable;
import org.modelmapper.ModelMapper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/genres")
@RequiredArgsConstructor
@Slf4j
public class PublicGenreController {

    private final GenreRepository genreRepository;
    private final ModelMapper modelMapper;

    @GetMapping
    @Cacheable(value = "genres")
    public ResponseEntity<List<GenreDTO>> getAllGenres() {
        log.info("[Public] Fetching all genres");
        List<GenreDTO> list = genreRepository.findAll().stream()
                .map(g -> modelMapper.map(g, GenreDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}
