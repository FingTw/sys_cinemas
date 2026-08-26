package com.example.cinema.admin.controllers;

import com.example.cinema.admin.dto.AdminProductDTO;
import com.example.cinema.admin.entities.ProductCategory;
import com.example.cinema.admin.entities.Product;
import com.example.cinema.admin.repositories.ProductCategoryRepository;
import com.example.cinema.admin.repositories.ProductRepository;
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
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@Slf4j
public class AdminProductController {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ADMIN', 'STAFF')")
    public ResponseEntity<List<AdminProductDTO>> getAllProducts() {
        log.info("[Admin] Lấy danh sách sản phẩm F&B");
        List<Product> entities = productRepository.findAllByIsDeletedFalse(Sort.by(Sort.Direction.ASC, "displayOrder"));
        List<AdminProductDTO> dtos = entities.stream().map(this::mapToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ADMIN', 'STAFF')")
    public ResponseEntity<AdminProductDTO> createProduct(@RequestBody AdminProductDTO dto) {
        log.info("[Admin] Tạo sản phẩm F&B: {}", dto.getName());
        ProductCategory category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục"));

        Product entity = Product.builder()
                .category(category)
                .name(dto.getName())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .price(dto.getPrice())
                .displayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 0)
                .active(dto.getActive() != null ? dto.getActive() : true)
                .isDeleted(false)
                .build();
        Product saved = productRepository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToDTO(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ADMIN', 'STAFF')")
    public ResponseEntity<AdminProductDTO> updateProduct(@PathVariable String id, @RequestBody AdminProductDTO dto) {
        log.info("[Admin] Cập nhật sản phẩm F&B: {}", id);
        return productRepository.findByIdAndIsDeletedFalse(id)
                .map(entity -> {
                    if (dto.getCategoryId() != null && !dto.getCategoryId().equals(entity.getCategory().getId())) {
                        ProductCategory category = categoryRepository.findById(dto.getCategoryId())
                                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục"));
                        entity.setCategory(category);
                    }
                    entity.setName(dto.getName());
                    entity.setDescription(dto.getDescription());
                    entity.setImageUrl(dto.getImageUrl());
                    entity.setPrice(dto.getPrice());
                    entity.setDisplayOrder(dto.getDisplayOrder());
                    entity.setActive(dto.getActive());
                    return productRepository.save(entity);
                })
                .map(this::mapToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ADMIN', 'STAFF')")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        log.info("[Admin] Xóa mềm sản phẩm F&B: {}", id);
        return productRepository.findByIdAndIsDeletedFalse(id)
                .map(entity -> {
                    entity.setIsDeleted(true);
                    productRepository.save(entity);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private AdminProductDTO mapToDTO(Product entity) {
        return AdminProductDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .imageUrl(entity.getImageUrl())
                .price(entity.getPrice())
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .categoryName(entity.getCategory() != null ? entity.getCategory().getName() : null)
                .displayOrder(entity.getDisplayOrder())
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
