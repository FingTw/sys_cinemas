package com.example.cinema.booking.application.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductDTO {
    private String id;
    private String name;
    private BigDecimal price;
    private String categoryId;
    private String categoryName;
    private Boolean active;
    private Integer displayOrder;
}
