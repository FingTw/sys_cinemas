package com.example.cinema.admin.controllers;

import com.example.cinema.admin.entities.CorsConfig;
import com.example.cinema.admin.services.AdminUserUseCaseImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminCorsConfigController {

    private final AdminUserUseCaseImpl adminUserUseCase;

    @Value("${app.security.internal-api-key:secret-key-123}")
    private String internalApiKey;

    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @GetMapping("/api/v1/admin/cors-config")
    public ResponseEntity<CorsConfig> getAdminConfig() {
        return ResponseEntity.ok(adminUserUseCase.getCorsConfig());
    }

    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @PutMapping("/api/v1/admin/cors-config")
    public ResponseEntity<CorsConfig> updateConfig(@RequestBody CorsConfig request) {
        return ResponseEntity.ok(adminUserUseCase.updateCorsConfig(request));
    }

    @GetMapping("/api/v1/internal/cors-config")
    public ResponseEntity<CorsConfig> getInternalConfig(@RequestHeader(value = "X-Internal-Api-Key", required = false) String apiKey) {
        if (!internalApiKey.equals(apiKey)) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(adminUserUseCase.getCorsConfig());
    }
}
