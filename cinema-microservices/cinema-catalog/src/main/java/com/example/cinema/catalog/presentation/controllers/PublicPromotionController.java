package com.example.cinema.catalog.presentation.controllers;

import com.example.cinema.catalog.application.dto.PromotionDTO;
import com.example.cinema.catalog.infrastructure.database.entities.PromotionJpaEntity;
import com.example.cinema.catalog.infrastructure.database.repositories.SpringDataPromotionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/public/promotions")
@RequiredArgsConstructor
@Slf4j
public class PublicPromotionController {

    private final SpringDataPromotionRepository promotionRepository;

    @GetMapping
    public ResponseEntity<List<PromotionDTO>> getPromotions() {
        log.info("[Public] Fetching active promotions sorted by displayOrder");
        List<PromotionJpaEntity> entities = promotionRepository.findAllByActiveTrueOrderByDisplayOrderAsc();
        
        List<PromotionDTO> dtos = entities.stream().map(entity -> PromotionDTO.builder()
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
