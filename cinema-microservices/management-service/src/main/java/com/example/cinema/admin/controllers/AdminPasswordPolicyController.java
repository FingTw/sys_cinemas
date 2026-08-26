package com.example.cinema.admin.controllers;

import com.example.cinema.admin.entities.PasswordPolicy;
import com.example.cinema.admin.services.AdminUserUseCaseImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/password-policy")
@RequiredArgsConstructor
public class AdminPasswordPolicyController {

    private final AdminUserUseCaseImpl adminUserUseCase;

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
