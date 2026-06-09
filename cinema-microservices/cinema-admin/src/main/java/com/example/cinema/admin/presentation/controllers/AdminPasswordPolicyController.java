package com.example.cinema.admin.presentation.controllers;

import com.example.cinema.admin.domain.entities.PasswordPolicy;
import com.example.cinema.admin.application.ports.in.AdminUserUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/password-policy")
@RequiredArgsConstructor
public class AdminPasswordPolicyController {

    private final AdminUserUseCase adminUserUseCase;

    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @GetMapping
    public ResponseEntity<PasswordPolicy> getPolicy() {
        return ResponseEntity.ok(adminUserUseCase.getPasswordPolicy());
    }

    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @PutMapping
    public ResponseEntity<PasswordPolicy> updatePolicy(@RequestBody PasswordPolicy request) {
        return ResponseEntity.ok(adminUserUseCase.updatePasswordPolicy(request));
    }
}
