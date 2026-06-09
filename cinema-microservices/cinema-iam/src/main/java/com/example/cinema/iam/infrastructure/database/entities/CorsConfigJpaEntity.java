package com.example.cinema.iam.infrastructure.database.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cors_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorsConfigJpaEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "allowed_origins")
    private String allowedOrigins;

    @Column(name = "allowed_methods")
    private String allowedMethods;

    @Column(name = "allowed_headers")
    private String allowedHeaders;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
