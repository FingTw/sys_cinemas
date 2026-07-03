package com.example.cinema.catalog.application.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ProductCategoryDTO {
    private String id;
    private String name;
    private String iconUrl;
    private Integer displayOrder;
    private Boolean active;
    private LocalDateTime createdAt;
}
