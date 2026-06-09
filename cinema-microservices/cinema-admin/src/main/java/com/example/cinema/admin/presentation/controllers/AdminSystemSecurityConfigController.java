package com.example.cinema.admin.presentation.controllers;

import com.example.cinema.admin.domain.entities.SystemSecurityConfig;
import com.example.cinema.admin.application.ports.in.AdminUserUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminSystemSecurityConfigController {

    private final AdminUserUseCase adminUserUseCase;

    @Value("${app.security.internal-api-key:secret-key-123}")
    private String internalApiKey;

    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @GetMapping("/api/v1/admin/security-config")
    public ResponseEntity<SystemSecurityConfig> getAdminConfig() {
        return ResponseEntity.ok(adminUserUseCase.getSystemSecurityConfig());
    }

    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @PutMapping("/api/v1/admin/security-config")
    public ResponseEntity<SystemSecurityConfig> updateConfig(@RequestBody SystemSecurityConfig request) {
        return ResponseEntity.ok(adminUserUseCase.updateSystemSecurityConfig(request));
    }

    @GetMapping("/api/v1/internal/security-config")
    public ResponseEntity<SystemSecurityConfig> getInternalConfig(@RequestHeader(value = "X-Internal-Api-Key", required = false) String apiKey) {
        if (!internalApiKey.equals(apiKey)) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(adminUserUseCase.getSystemSecurityConfig());
    }
}
