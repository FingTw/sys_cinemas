package com.example.cinema.admin.presentation.controllers;

import com.example.cinema.admin.application.dto.AdminBannerDTO;
import com.example.cinema.admin.infrastructure.database.entities.BannerJpaEntity;
import com.example.cinema.admin.infrastructure.database.repositories.SpringDataBannerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/banners")
@RequiredArgsConstructor
@Slf4j
public class AdminBannerController {

    private final SpringDataBannerRepository bannerRepository;
    private final StringRedisTemplate redisTemplate;

    private void clearCache() {
        redisTemplate.delete("cache:home-overview");
        log.info("[CINEMA-ADMIN] Cleared cache:home-overview in Redis due to banner changes");
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MOVIE_READ')")
    public ResponseEntity<List<AdminBannerDTO>> getAllBanners() {
        log.info("[CINEMA-ADMIN] Fetching all advertisement banners");
        List<BannerJpaEntity> entities = bannerRepository.findAllByOrderByDisplayOrderAsc();
        List<AdminBannerDTO> dtos = entities.stream().map(this::mapToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MOVIE_UPDATE')")
    public ResponseEntity<?> addBanner(@RequestBody AdminBannerDTO requestDto) {
        log.info("[CINEMA-ADMIN] Adding new advertisement banner: {}", requestDto.getTitle());
        
        if (requestDto.getTitle() == null || requestDto.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Title is required");
        }
        if (requestDto.getImageUrl() == null || requestDto.getImageUrl().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Image URL is required");
        }

        // Find max display_order
        List<BannerJpaEntity> all = bannerRepository.findAllByOrderByDisplayOrderAsc();
        int nextOrder = all.isEmpty() ? 1 : all.get(all.size() - 1).getDisplayOrder() + 1;

        BannerJpaEntity newBanner = BannerJpaEntity.builder()
            .title(requestDto.getTitle().trim())
            .imageUrl(requestDto.getImageUrl().trim())
            .linkUrl(requestDto.getLinkUrl() != null ? requestDto.getLinkUrl().trim() : "")
            .displayOrder(nextOrder)
            .active(true)
            .createdAt(LocalDateTime.now())
            .build();

        bannerRepository.save(newBanner);
        clearCache();
        return ResponseEntity.ok(mapToDTO(newBanner));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MOVIE_UPDATE')")
    public ResponseEntity<?> deleteBanner(@PathVariable String id) {
        log.info("[CINEMA-ADMIN] Deleting advertisement banner: {}", id);
        if (!bannerRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        bannerRepository.deleteById(id);
        clearCache();
        return ResponseEntity.ok().build();
    }

    @PutMapping("/reorder")
    @PreAuthorize("hasAuthority('MOVIE_UPDATE')")
    public ResponseEntity<?> reorderBanners(@RequestBody List<String> orderedIds) {
        log.info("[CINEMA-ADMIN] Reordering banners: {}", orderedIds);
        
        List<BannerJpaEntity> entities = bannerRepository.findAll();
        for (BannerJpaEntity entity : entities) {
            int index = orderedIds.indexOf(entity.getId());
            if (index != -1) {
                entity.setDisplayOrder(index + 1);
                bannerRepository.save(entity);
            }
        }
        clearCache();
        return ResponseEntity.ok().build();
    }

    private AdminBannerDTO mapToDTO(BannerJpaEntity entity) {
        return AdminBannerDTO.builder()
            .id(entity.getId())
            .title(entity.getTitle())
            .imageUrl(entity.getImageUrl())
            .linkUrl(entity.getLinkUrl())
            .displayOrder(entity.getDisplayOrder())
            .active(entity.getActive())
            .createdAt(entity.getCreatedAt())
            .build();
    }
}
