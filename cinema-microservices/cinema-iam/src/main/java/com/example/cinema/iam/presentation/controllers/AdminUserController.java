package com.example.cinema.iam.presentation.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;
import java.util.stream.Collectors;

import com.example.cinema.iam.application.dto.AdminUserDTO;
import com.example.cinema.iam.application.usecases.AdminUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private static final Logger log = LoggerFactory.getLogger(AdminUserController.class);

    @Autowired
    private AdminUserService adminUserService;

    @PreAuthorize("hasAuthority('USER_READ')")
    @GetMapping
    public ResponseEntity<List<AdminUserDTO>> getAllUsers() {
        log.info("Admin requested to fetch all users");
        try {
            return ResponseEntity.ok(adminUserService.getAllUsers());
        } catch (Exception e) {
            log.error("Failed to fetch users: {}", e.getMessage());
            throw e;
        }
    }

    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @PutMapping("/{id}/role")
    public ResponseEntity<?> changeRole(@PathVariable String id, @RequestBody Map<String, Object> body) {
        Object roleObj = body.get("role");
        String rolesString = "";
        if (roleObj instanceof List) {
            rolesString = ((List<?>) roleObj).stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(","));
        } else if (roleObj != null) {
            rolesString = roleObj.toString();
        }

        log.info("Admin changing roles for User [{}] to [{}]: ", id, rolesString);
        try {
            adminUserService.changeRole(id, rolesString);
            return ResponseEntity.ok(Map.of("message", "Role changed successfully"));
        } catch (Exception e) {
            log.error("Failed to change role for user [{}]: {}", id, e.getMessage());
            throw e;
        }
    }

    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @PutMapping("/{id}/block")
    public ResponseEntity<?> blockUser(@PathVariable String id) {
        log.info("Admin blocking/unblocking User [{}]: ", id);
        try {
            adminUserService.blockUser(id);
            return ResponseEntity.ok(Map.of("message", "User block status updated successfully"));
        } catch (Exception e) {
            log.error("Failed to update block status for user [{}]: {}", id, e.getMessage());
            throw e;
        }
    }

    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @PutMapping("/{id}/kick")
    public ResponseEntity<?> kickUser(@PathVariable String id) {
        log.info("Admin kicking User [{}] from system: ", id);
        try {
            adminUserService.kickUser(id);
            return ResponseEntity.ok(Map.of("message", "User kicked successfully"));
        } catch (Exception e) {
            log.error("Failed to kick user [{}]: {}", id, e.getMessage());
            throw e;
        }
    }

    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @GetMapping("/permissions")
    public ResponseEntity<?> getAllPermissions() {
        log.info("Admin requested to fetch all available permissions");
        return ResponseEntity.ok(adminUserService.getAllPermissions());
    }

    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @PutMapping("/{id}/permissions")
    public ResponseEntity<?> changePermissions(@PathVariable String id, @RequestBody List<String> permissions) {
        log.info("Admin changing permissions for User [{}] to: {}", id, permissions);
        try {
            adminUserService.changePermissions(id, permissions);
            return ResponseEntity.ok(Map.of("message", "Permissions updated successfully"));
        } catch (Exception e) {
            log.error("Failed to update permissions for user [{}]: {}", id, e.getMessage());
            throw e;
        }
    }
}
