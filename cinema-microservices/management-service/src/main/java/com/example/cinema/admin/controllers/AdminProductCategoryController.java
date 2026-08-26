package com.example.cinema.admin.controllers;

import com.example.cinema.admin.dto.AdminProductCategoryDTO;
import com.example.cinema.admin.entities.ProductCategory;
import com.example.cinema.admin.repositories.ProductCategoryRepository;
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
@RequestMapping("/api/v1/admin/product-categories")
@RequiredArgsConstructor
@Slf4j
public class AdminProductCategoryController {

    private final ProductCategoryRepository categoryRepository;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ADMIN', 'STAFF')")
    public ResponseEntity<List<AdminProductCategoryDTO>> getAllCategories() {
        log.info("[Admin] Lấy danh sách danh mục sản phẩm F&B");
        List<ProductCategory> entities = categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "displayOrder"));
        List<AdminProductCategoryDTO> dtos = entities.stream().map(this::mapToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ADMIN', 'STAFF')")
    public ResponseEntity<AdminProductCategoryDTO> createCategory(@RequestBody AdminProductCategoryDTO dto) {
        log.info("[Admin] Tạo danh mục sản phẩm F&B: {}", dto.getName());
        ProductCategory entity = ProductCategory.builder()
                .name(dto.getName())
                .iconUrl(dto.getIconUrl())
                .displayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 0)
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();
        ProductCategory saved = categoryRepository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToDTO(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ADMIN', 'STAFF')")
    public ResponseEntity<AdminProductCategoryDTO> updateCategory(@PathVariable String id, @RequestBody AdminProductCategoryDTO dto) {
        log.info("[Admin] Cập nhật danh mục sản phẩm F&B: {}", id);
        return categoryRepository.findById(id)
                .map(entity -> {
                    entity.setName(dto.getName());
                    entity.setIconUrl(dto.getIconUrl());
                    entity.setDisplayOrder(dto.getDisplayOrder());
                    entity.setActive(dto.getActive());
                    return categoryRepository.save(entity);
                })
                .map(this::mapToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ADMIN', 'STAFF')")
    public ResponseEntity<Void> deleteCategory(@PathVariable String id) {
        log.info("[Admin] Xóa danh mục sản phẩm F&B: {}", id);
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private AdminProductCategoryDTO mapToDTO(ProductCategory entity) {
        return AdminProductCategoryDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .iconUrl(entity.getIconUrl())
                .displayOrder(entity.getDisplayOrder())
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
