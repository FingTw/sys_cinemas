package com.example.cinema.iam.presentation.controllers;

import com.example.cinema.iam.application.dto.UpdateProfileRequest;
import com.example.cinema.iam.application.dto.UserProfileDTO;
import com.example.cinema.iam.application.ports.in.UserProfileUseCase;
import com.example.cinema.common.security.JwtTokenProvider;
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

    private final UserProfileUseCase userProfileUseCase;

    public UserProfileController(UserProfileUseCase userProfileUseCase) {
        this.userProfileUseCase = userProfileUseCase;
    }

    /**
     * Xem thông tin cá nhân (dựa trên userId từ JWT).
     */
    @GetMapping
    public ResponseEntity<UserProfileDTO> getMyProfile(
            @RequestHeader("Authorization") String authHeader) {
        String userId = extractUserIdFromHeader(authHeader);
        return ResponseEntity.ok(userProfileUseCase.getProfile(userId));
    }

    /**
     * Cập nhật thông tin cá nhân (hiện tại chỉ cho phép đổi email).
     */
    @PutMapping
    public ResponseEntity<UserProfileDTO> updateMyProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UpdateProfileRequest request) {
        String userId = extractUserIdFromHeader(authHeader);
        return ResponseEntity.ok(userProfileUseCase.updateProfile(userId, request));
    }

    /**
     * Xem thông tin cá nhân của user bất kỳ bằng ID (phục vụ gọi nội bộ/admin).
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileDTO> getUserProfileById(@PathVariable String id) {
        return ResponseEntity.ok(userProfileUseCase.getProfile(id));
    }

    /**
     * Trích xuất userId từ JWT token trong Authorization header.
     * Token format: "Bearer <jwt>"
     */
    private String extractUserIdFromHeader(String authHeader) {
        // Parse JWT payload thủ công để lấy userId mà không cần inject JwtTokenProvider
        String token = authHeader.replace("Bearer ", "");
        try {
            String[] parts = token.split("\\.");
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            // Parse JSON thủ công: tìm "userId":"<value>"
            int idx = payload.indexOf("\"userId\"");
            if (idx >= 0) {
                int start = payload.indexOf("\"", idx + 8) + 1;
                int end = payload.indexOf("\"", start);
                return payload.substring(start, end);
            }
        } catch (Exception e) {
            throw new RuntimeException("Khong the trich xuat userId tu Token.");
        }
        throw new RuntimeException("Token khong chua userId.");
    }
}
