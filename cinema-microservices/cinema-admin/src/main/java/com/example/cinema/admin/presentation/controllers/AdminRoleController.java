package com.example.cinema.admin.presentation.controllers;

import com.example.cinema.admin.application.dto.RoleDTO;
import com.example.cinema.admin.application.ports.in.AdminUserUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/admin/roles")
@RequiredArgsConstructor
@Slf4j
public class AdminRoleController {

    private final AdminUserUseCase adminUserUseCase;

    @PreAuthorize("hasAuthority('USER_READ')")
    @GetMapping
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        log.info("Admin requested to fetch all roles");
        return ResponseEntity.ok(adminUserUseCase.getAllRoles());
    }

    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @PostMapping
    public ResponseEntity<RoleDTO> createRole(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        @SuppressWarnings("unchecked")
        List<String> permissions = (List<String>) body.get("permissions");
        Set<String> permissionSet = permissions != null ? Set.copyOf(permissions) : Set.of();
        log.info("Admin requested to create new role: [{}] with permissions: {}", name, permissionSet);
        return ResponseEntity.ok(adminUserUseCase.createRole(name, permissionSet));
    }

    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @PutMapping("/{id}/permissions")
    public ResponseEntity<RoleDTO> updateRolePermissions(@PathVariable UUID id, @RequestBody List<String> permissions) {
        Set<String> permissionSet = permissions != null ? Set.copyOf(permissions) : Set.of();
        log.info("Admin requested to update permissions for role ID: [{}] to: {}", id, permissionSet);
        return ResponseEntity.ok(adminUserUseCase.updateRolePermissions(id, permissionSet));
    }

    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable UUID id) {
        log.info("Admin requested to delete role ID: [{}]", id);
        adminUserUseCase.deleteRole(id);
        return ResponseEntity.ok(Map.of("message", "Role deleted successfully"));
    }
}
