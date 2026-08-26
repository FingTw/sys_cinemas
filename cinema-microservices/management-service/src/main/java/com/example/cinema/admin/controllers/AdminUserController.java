package com.example.cinema.admin.controllers;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.cinema.admin.dto.AdminUserDTO;
import com.example.cinema.admin.services.AdminUserUseCaseImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

    private final AdminUserUseCaseImpl adminUserUseCase;

    @PreAuthorize("hasAuthority('USER_READ')")
    @GetMapping
    public ResponseEntity<List<AdminUserDTO>> getAllUsers() {
        log.info("Admin requested to fetch all users");
        return ResponseEntity.ok(adminUserUseCase.getAllUsers());
    }

    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @PutMapping("/{id}/role")
    public ResponseEntity<?> changeRole(@PathVariable String id, @RequestBody Map<String, Object> body) {
        Object roleObj = body.get("role");
        String rolesString = "";
        if (roleObj instanceof List) {
            rolesString = ((List<?>) roleObj).stream()
                    .map(Object::toString)
                    .collect(java.util.stream.Collectors.joining(","));
        } else if (roleObj != null) {
            rolesString = roleObj.toString();
        }

        log.info("Admin changing roles for User [{}] to [{}]", id, rolesString);
        adminUserUseCase.changeRole(id, rolesString);
        return ResponseEntity.ok(Map.of("message", "Role changed successfully"));
    }

    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @PutMapping("/{id}/block")
    public ResponseEntity<?> blockUser(@PathVariable String id) {
        log.info("Admin blocking/unblocking User [{}]", id);
        adminUserUseCase.blockUser(id);
        return ResponseEntity.ok(Map.of("message", "User block status updated successfully"));
    }

    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @PutMapping("/{id}/kick")
    public ResponseEntity<?> kickUser(@PathVariable String id) {
        log.info("Admin kicking User [{}] from system", id);
        adminUserUseCase.kickUser(id);
        return ResponseEntity.ok(Map.of("message", "User kicked successfully"));
    }

    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @PutMapping("/{id}/workplace")
    public ResponseEntity<?> assignWorkplace(@PathVariable String id, @RequestBody Map<String, String> body) {
        String cinemaId = body.get("cinemaId");
        log.info("Admin assigning workplace [{}] to User [{}]", cinemaId, id);
        adminUserUseCase.assignWorkplace(id, cinemaId);
        return ResponseEntity.ok(Map.of("message", "Workplace assigned successfully"));
    }

    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @GetMapping("/permissions")
    public ResponseEntity<?> getAllPermissions() {
        log.info("Admin requested to fetch all available permissions");
        return ResponseEntity.ok(adminUserUseCase.getAllPermissions());
    }
}
