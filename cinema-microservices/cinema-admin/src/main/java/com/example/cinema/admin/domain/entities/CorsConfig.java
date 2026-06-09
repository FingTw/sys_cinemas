package com.example.cinema.admin.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorsConfig {
    private String id;
    private String allowedOrigins;
    private String allowedMethods;
    private String allowedHeaders;
    private LocalDateTime updatedAt;
}
