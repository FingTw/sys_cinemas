package com.example.cinema.admin.controllers;

import com.example.cinema.admin.dto.AdminPromotionDTO;
import com.example.cinema.admin.entities.Promotion;
import com.example.cinema.admin.repositories.PromotionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/promotions")
@RequiredArgsConstructor
@Slf4j
public class AdminPromotionController {

    private final PromotionRepository promotionRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ') or hasAuthority('USER_MANAGE')")
    public ResponseEntity<List<AdminPromotionDTO>> getAllPromotions() {
        log.info("[Admin] Lấy danh sách promotions");
        List<Promotion> entities = promotionRepository.findAll(Sort.by(Sort.Direction.ASC, "displayOrder"));
        
        List<AdminPromotionDTO> dtos = entities.stream().map(this::mapToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_READ') or hasAuthority('USER_MANAGE')")
    public ResponseEntity<AdminPromotionDTO> getPromotionById(@PathVariable String id) {
        return promotionRepository.findById(id)
                .map(this::mapToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<AdminPromotionDTO> createPromotion(@RequestBody AdminPromotionDTO dto) {
        log.info("[Admin] Tạo promotion mới: {}", dto.getTitle());
        Promotion entity = Promotion.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .linkUrl(dto.getLinkUrl())
                .displayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 0)
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();
                
        Promotion saved = promotionRepository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToDTO(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<AdminPromotionDTO> updatePromotion(@PathVariable String id, @RequestBody AdminPromotionDTO dto) {
        log.info("[Admin] Cập nhật promotion: {}", id);
        return promotionRepository.findById(id)
                .map(entity -> {
                    entity.setTitle(dto.getTitle());
                    entity.setDescription(dto.getDescription());
                    entity.setImageUrl(dto.getImageUrl());
                    entity.setLinkUrl(dto.getLinkUrl());
                    entity.setDisplayOrder(dto.getDisplayOrder());
                    entity.setActive(dto.getActive());
                    return promotionRepository.save(entity);
                })
                .map(this::mapToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<Void> deletePromotion(@PathVariable String id) {
        log.info("[Admin] Xóa promotion: {}", id);
        if (promotionRepository.existsById(id)) {
            promotionRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private AdminPromotionDTO mapToDTO(Promotion entity) {
        return AdminPromotionDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .imageUrl(entity.getImageUrl())
                .linkUrl(entity.getLinkUrl())
                .displayOrder(entity.getDisplayOrder())
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
