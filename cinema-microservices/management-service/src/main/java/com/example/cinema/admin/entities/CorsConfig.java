package com.example.cinema.admin.entities;

import jakarta.persistence.Column;
import jakarta.persistence.*;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "cors_configs", schema = "auth")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorsConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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
