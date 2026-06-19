package com.example.cinema.catalog.presentation.controllers;

import com.example.cinema.catalog.application.dto.ServiceDTO;
import com.example.cinema.catalog.infrastructure.database.entities.ServiceJpaEntity;
import com.example.cinema.catalog.infrastructure.database.repositories.SpringDataServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/public/services")
@RequiredArgsConstructor
@Slf4j
public class PublicServiceController {

    private final SpringDataServiceRepository serviceRepository;

    @GetMapping
    public ResponseEntity<List<ServiceDTO>> getServices() {
        log.info("[Public] Fetching active services sorted by displayOrder");
        List<ServiceJpaEntity> entities = serviceRepository.findAllByActiveTrueOrderByDisplayOrderAsc();
        
        List<ServiceDTO> dtos = entities.stream().map(entity -> ServiceDTO.builder()
            .id(entity.getId())
            .title(entity.getTitle())
            .description(entity.getDescription())
            .imageUrl(entity.getImageUrl())
            .linkUrl(entity.getLinkUrl())
            .displayOrder(entity.getDisplayOrder())
            .createdAt(entity.getCreatedAt())
            .build()
        ).collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
}
