package com.example.cinema.catalog.presentation.controllers;

import com.example.cinema.catalog.application.dto.ProductCategoryDTO;
import com.example.cinema.catalog.application.dto.ProductDTO;
import com.example.cinema.catalog.infrastructure.database.entities.ProductCategoryJpaEntity;
import com.example.cinema.catalog.infrastructure.database.entities.ProductJpaEntity;
import com.example.cinema.catalog.infrastructure.database.repositories.SpringDataProductCategoryRepository;
import com.example.cinema.catalog.infrastructure.database.repositories.SpringDataProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/public/products")
@RequiredArgsConstructor
@Slf4j
public class PublicProductController {

    private final SpringDataProductRepository productRepository;
    private final SpringDataProductCategoryRepository categoryRepository;

    /**
     * Lấy tất cả sản phẩm F&B đang hoạt động.
     */
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getProducts() {
        log.info("[Public] Fetching active products");
        List<ProductJpaEntity> entities = productRepository.findAllByActiveTrueAndIsDeletedFalseOrderByDisplayOrderAsc();
        List<ProductDTO> dtos = entities.stream().map(this::mapToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Lấy danh sách danh mục sản phẩm.
     */
    @GetMapping("/categories")
    public ResponseEntity<List<ProductCategoryDTO>> getCategories() {
        log.info("[Public] Fetching active product categories");
        List<ProductCategoryJpaEntity> entities = categoryRepository.findAllByActiveTrueOrderByDisplayOrderAsc();
        List<ProductCategoryDTO> dtos = entities.stream().map(this::mapCategoryToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Lấy chi tiết 1 sản phẩm.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable String id) {
        return productRepository.findByIdAndIsDeletedFalse(id)
                .map(this::mapToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private ProductDTO mapToDTO(ProductJpaEntity entity) {
        return ProductDTO.builder()
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
                .build();
    }

    private ProductCategoryDTO mapCategoryToDTO(ProductCategoryJpaEntity entity) {
        return ProductCategoryDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .iconUrl(entity.getIconUrl())
                .displayOrder(entity.getDisplayOrder())
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
