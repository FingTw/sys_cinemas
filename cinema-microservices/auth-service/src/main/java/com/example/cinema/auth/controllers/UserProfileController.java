package com.example.cinema.auth.controllers;

import com.example.cinema.auth.dto.UpdateProfileRequest;
import com.example.cinema.auth.dto.UserProfileDTO;
import com.example.cinema.auth.services.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller cho User tự xem và cập nhật thông tin cá nhân.
 * Endpoint: /api/v1/profile
 * Yêu cầu: Đã đăng nhập (authenticated).
 */
@RestController
@RequestMapping("/api/v1/profile")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    /**
     * Xem thông tin cá nhân (dựa trên userId từ JWT).
     */
    @GetMapping
    public ResponseEntity<UserProfileDTO> getMyProfile(org.springframework.security.core.Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(userProfileService.getProfile(userId));
    }

    @PutMapping
    public ResponseEntity<UserProfileDTO> updateMyProfile(
            org.springframework.security.core.Authentication authentication,
            @RequestBody UpdateProfileRequest request) {
        String userId = authentication.getName();
        return ResponseEntity.ok(userProfileService.updateProfile(userId, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileDTO> getUserProfileById(@PathVariable String id) {
        return ResponseEntity.ok(userProfileService.getProfile(id));
    }
}
