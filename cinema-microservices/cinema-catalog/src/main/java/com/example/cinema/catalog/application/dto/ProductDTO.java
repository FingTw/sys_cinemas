package com.example.cinema.catalog.application.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ProductDTO {
    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private String categoryId;
    private String categoryName;
    private Integer displayOrder;
    private Boolean active;
    private LocalDateTime createdAt;
}
