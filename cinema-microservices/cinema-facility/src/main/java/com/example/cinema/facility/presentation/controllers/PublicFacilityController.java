package com.example.cinema.facility.presentation.controllers;

import com.example.cinema.facility.application.dto.CinemaComplexDTO;
import com.example.cinema.facility.application.dto.CinemaDTO;
import com.example.cinema.facility.domain.entities.Cinema;
import com.example.cinema.facility.domain.entities.CinemaComplex;
import com.example.cinema.facility.domain.repositories.CinemaComplexRepository;
import com.example.cinema.facility.domain.repositories.CinemaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.cache.annotation.Cacheable;
import org.modelmapper.ModelMapper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/facilities")
@RequiredArgsConstructor
@Slf4j
public class PublicFacilityController {

    private final CinemaComplexRepository cinemaComplexRepository;
    private final CinemaRepository cinemaRepository;
    private final ModelMapper modelMapper;

    @GetMapping("/complexes")
    @Cacheable(value = "cinemaComplexes")
    public ResponseEntity<List<CinemaComplexDTO>> getAllComplexes() {
        log.info("[Public] Fetching all cinema complexes");
        List<CinemaComplexDTO> list = cinemaComplexRepository.findAll().stream()
                .map(cc -> modelMapper.map(cc, CinemaComplexDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/complexes/{complexId}/cinemas")
    @Cacheable(value = "cinemasByComplex", key = "#complexId")
    public ResponseEntity<List<CinemaDTO>> getCinemasByComplex(@PathVariable String complexId) {
        log.info("[Public] Fetching cinemas for complex ID: [{}]", complexId);
        List<CinemaDTO> list = cinemaRepository.findByComplexId(complexId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/cinemas")
    @Cacheable(value = "cinemas")
    public ResponseEntity<List<CinemaDTO>> getAllCinemas() {
        log.info("[Public] Fetching all cinemas");
        List<CinemaDTO> list = cinemaRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/cinemas/{cinemaId}")
    @Cacheable(value = "cinema", key = "#cinemaId")
    public ResponseEntity<CinemaDTO> getCinemaById(@PathVariable String cinemaId) {
        log.info("[Public] Fetching cinema ID: [{}]", cinemaId);
        Cinema cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new IllegalArgumentException("Cinema not found with ID: " + cinemaId));
        return ResponseEntity.ok(convertToDTO(cinema));
    }

    private CinemaDTO convertToDTO(Cinema cinema) {
        CinemaDTO dto = modelMapper.map(cinema, CinemaDTO.class);
        cinemaComplexRepository.findById(cinema.getComplexId()).ifPresent(cc -> {
            dto.setComplexName(cc.getName());
        });
        return dto;
    }
}
