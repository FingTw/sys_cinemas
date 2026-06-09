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
@Table(name = "security_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemSecurityConfigJpaEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "client_key", nullable = false)
    private String clientKey;

    @Column(name = "gateway_protected_paths", nullable = false, length = 1000)
    private String gatewayProtectedPaths;

    @Column(name = "service_bypass_paths", nullable = false, length = 1000)
    private String serviceBypassPaths;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
