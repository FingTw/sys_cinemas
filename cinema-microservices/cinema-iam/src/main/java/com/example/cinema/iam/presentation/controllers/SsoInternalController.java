package com.example.cinema.iam.presentation.controllers;

import com.example.cinema.iam.domain.entities.User;
import com.example.cinema.iam.domain.repositories.UserRepository;
import com.example.cinema.iam.domain.repositories.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * SsoInternalController - Endpoint nội bộ tiếp nhận các sự kiện đồng bộ từ Keycloak Event Listener SPI.
 * Chỉ cho phép gọi nội bộ qua việc xác thực X-Internal-Api-Key.
 */
@RestController
@RequestMapping("/api/v1/internal/sso")
@RequiredArgsConstructor
@Slf4j
public class SsoInternalController {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final StringRedisTemplate redisTemplate;

    @Value("${APP_SECURITY_INTERNAL_API_KEY:internal-secret-key}")
    private String internalApiKey;

    /**
     * Đồng bộ sự kiện người dùng từ Keycloak (ví dụ: Admin xóa, khóa hoặc mở khóa tài khoản).
     *
     * @param apiKey  Mã xác thực nội bộ được gửi trong header X-Internal-Api-Key.
     * @param payload Payload JSON chứa loại sự kiện và ssoSubject (Keycloak sub UUID).
     */
    @PostMapping("/user-event")
    public ResponseEntity<?> handleUserEvent(
            @RequestHeader(value = "X-Internal-Api-Key", required = false) String apiKey,
            @RequestBody Map<String, String> payload) {

        log.info("[SSO-Event] Nhận event đồng bộ từ Keycloak: {}", payload);

        // 1. Xác thực API Key nội bộ
        if (apiKey == null || !internalApiKey.equals(apiKey)) {
            log.warn("[SSO-Event] Cố gắng truy cập trái phép vào endpoint nội bộ. ApiKey nhận được: {}", apiKey);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Invalid or missing Internal API Key"));
        }

        String type = payload.get("type");
        String ssoSubject = payload.get("ssoSubject");

        if (ssoSubject == null || ssoSubject.trim().isEmpty() || type == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing type or ssoSubject"));
        }

        // 2. Tìm người dùng theo ssoSubject
        return userRepository.findBySsoSubject(ssoSubject)
                .map(user -> {
                    if ("USER_DISABLE".equals(type)) {
                        log.info("[SSO-Event] Keycloak event USER_DISABLE -> Khóa tài khoản [{}] (sub: {})", 
                                user.getUsername(), ssoSubject);
                        
                        // Cập nhật trạng thái trong database
                        user.block();
                        userRepository.save(user);

                        // Hủy bỏ phiên làm việc và xóa cache token
                        redisTemplate.delete("valid_token:" + user.getId());
                        redisTemplate.delete("user_auth:" + user.getId());
                        tokenRepository.revokeAllByUserId(user.getId());
                        
                        return ResponseEntity.ok(Map.of("message", "User blocked successfully", "username", user.getUsername()));
                    } else if ("USER_ENABLE".equals(type)) {
                        log.info("[SSO-Event] Keycloak event USER_ENABLE -> Kích hoạt lại tài khoản [{}] (sub: {})", 
                                user.getUsername(), ssoSubject);
                        
                        // Mở khóa tài khoản
                        user.unblock();
                        userRepository.save(user);
                        
                        return ResponseEntity.ok(Map.of("message", "User unblocked successfully", "username", user.getUsername()));
                    } else {
                        return ResponseEntity.badRequest().body(Map.of("error", "Unsupported event type: " + type));
                    }
                })
                .orElseGet(() -> {
                    log.warn("[SSO-Event] Nhận event cho user có ssoSubject [{}] nhưng không tìm thấy trong database.", ssoSubject);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "User not found with ssoSubject: " + ssoSubject));
                });
    }
}
