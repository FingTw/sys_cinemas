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
public class SystemSecurityConfig {
    private String id;
    private String clientKey;
    private String gatewayProtectedPaths;
    private String serviceBypassPaths;
    private LocalDateTime updatedAt;
}
