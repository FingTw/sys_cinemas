package com.example.cinema.catalog.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BannerDTO {
    private String id;
    private String title;
    private String imageUrl;
    private String linkUrl;
    private Integer displayOrder;
    private LocalDateTime createdAt;
}
