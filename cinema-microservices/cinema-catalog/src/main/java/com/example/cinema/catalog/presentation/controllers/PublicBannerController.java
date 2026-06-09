package com.example.cinema.catalog.presentation.controllers;

import com.example.cinema.catalog.application.dto.BannerDTO;
import com.example.cinema.catalog.infrastructure.database.entities.BannerJpaEntity;
import com.example.cinema.catalog.infrastructure.database.repositories.SpringDataBannerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/movies/banners")
@RequiredArgsConstructor
@Slf4j
public class PublicBannerController {

    private final SpringDataBannerRepository bannerRepository;

    @GetMapping
    public ResponseEntity<List<BannerDTO>> getBanners() {
        log.info("[Public] Fetching active advertisement banners sorted by displayOrder");
        List<BannerJpaEntity> entities = bannerRepository.findAllByActiveTrueOrderByDisplayOrderAsc();
        
        List<BannerDTO> dtos = entities.stream().map(entity -> BannerDTO.builder()
            .id(entity.getId())
            .title(entity.getTitle())
            .imageUrl(entity.getImageUrl())
            .linkUrl(entity.getLinkUrl())
            .displayOrder(entity.getDisplayOrder())
            .createdAt(entity.getCreatedAt())
            .build()
        ).collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
}
