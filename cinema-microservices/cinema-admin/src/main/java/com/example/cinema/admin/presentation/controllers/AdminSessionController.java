package com.example.cinema.admin.presentation.controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.cinema.admin.application.dto.AuthTokenDTO;
import com.example.cinema.admin.application.ports.in.AdminUserUseCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/admin/sessions")
@RequiredArgsConstructor
@Slf4j
public class AdminSessionController {

    private final AdminUserUseCase adminUserUseCase;

    @PreAuthorize("hasAuthority('USER_READ')")
    @GetMapping
    public ResponseEntity<List<AuthTokenDTO>> getAllSessions() {
        log.info("Admin requested to fetch all sessions");
        return ResponseEntity.ok(adminUserUseCase.getAllSessions());
    }

    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @PutMapping("/{id}/revoke")
    public ResponseEntity<?> revokeSession(@PathVariable UUID id) {
        log.info("Admin revoking session [{}]", id);
        adminUserUseCase.revokeSession(id);
        return ResponseEntity.ok(Map.of("message", "Session revoked successfully"));
    }
}
