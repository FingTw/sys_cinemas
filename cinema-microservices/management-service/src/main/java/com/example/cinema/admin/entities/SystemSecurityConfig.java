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
@Table(name = "security_configs", schema = "auth")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemSecurityConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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
