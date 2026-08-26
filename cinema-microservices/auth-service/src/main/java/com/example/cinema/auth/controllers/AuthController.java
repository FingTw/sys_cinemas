package com.example.cinema.auth.controllers;

import com.example.cinema.auth.dto.RegisterRequest;
import com.example.cinema.auth.services.RegisterUserService;
import com.example.cinema.auth.repositories.UserRepository;
import com.example.cinema.auth.entities.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUserService registerUserService;
    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public AuthController(RegisterUserService registerUserService, UserRepository userRepository, StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.registerUserService = registerUserService;
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        registerUserService.register(request);
        return ResponseEntity.ok(Map.of("message", "Registration successful"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        if (authentication != null && authentication.isAuthenticated()) {
            String userId = authentication.getName();
            redisTemplate.delete("user_perms:" + userId);
            redisTemplate.delete("online:" + userId);
            
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String jwt = authHeader.substring(7);
                    String[] parts = jwt.split("\\.");
                    if (parts.length >= 2) {
                        String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                        com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(payload);
                        if (jsonNode.has("jti")) {
                            redisTemplate.delete("active_session:" + jsonNode.get("jti").asText());
                        }
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
        
        Cookie accessCookie = new Cookie("ACCESS_TOKEN", null);
        accessCookie.setMaxAge(0);
        accessCookie.setPath("/");
        response.addCookie(accessCookie);
        
        Cookie refreshCookie = new Cookie("REFRESH_TOKEN", null);
        refreshCookie.setMaxAge(0);
        refreshCookie.setPath("/api/v1/auth/refresh-token");
        response.addCookie(refreshCookie);
        
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication, jakarta.servlet.http.HttpServletRequest request) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        
        String userId = authentication.getName();
        String username = userId;
        String email = "";
        
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            try {
                String[] parts = jwt.split("\\.");
                if (parts.length >= 2) {
                    String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                    com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(payload);
                    if (jsonNode.has("preferred_username")) {
                        username = jsonNode.get("preferred_username").asText();
                    }
                    if (jsonNode.has("email")) {
                        email = jsonNode.get("email").asText();
                    }
                }
            } catch (Exception e) {
                // Ignore payload parse error
            }
        }
        
        // 1. Resolve User in Local DB
        User localUser = userRepository.findBySsoSubject(userId).orElse(null);
        if (localUser == null) {
            localUser = userRepository.findByUsername(username).orElse(null);
        }
        if (localUser == null && email != null && !email.isEmpty()) {
            localUser = userRepository.findByEmail(email).orElse(null);
        }

        if (localUser == null) {
            localUser = User.builder()
                .id(userId)
                .username(username)
                .email(email != null && !email.isEmpty() ? email : username + "@dummy.com")
                .isBlocked(false)
                .ssoSubject(userId)
                .build();
            userRepository.save(localUser);
        } else if (localUser.getSsoSubject() == null || !localUser.getSsoSubject().equals(userId)) {
            localUser.setSsoSubject(userId);
            userRepository.save(localUser);
        }

        String dbUserId = localUser.getId();

        // 2. Fetch roles and permissions from Local DB
        List<String> roles = userRepository.findRolesByUserId(dbUserId);
        List<String> permissions = userRepository.findPermissionsByUserId(dbUserId);
        
        System.out.println("DEBUG - SSO userId: " + userId);
        System.out.println("DEBUG - DB userId: " + dbUserId);
        System.out.println("DEBUG - Found roles: " + roles);
        System.out.println("DEBUG - Found permissions: " + permissions);

        String rolesString = String.join(",", roles);
        String permissionsString = String.join(",", permissions);

        // 3. Cache to Redis for Gateway
        try {
            Map<String, String> cacheData = new HashMap<>();
            cacheData.put("roles", rolesString);
            cacheData.put("permissions", permissionsString);
            redisTemplate.opsForValue().set("user_perms:" + userId, objectMapper.writeValueAsString(cacheData), 1, TimeUnit.HOURS);
            
            // Record online status
            redisTemplate.opsForValue().set("online:" + userId, "true", 30, TimeUnit.MINUTES);
            
            // Record active session
            String jti = java.util.UUID.randomUUID().toString();
            long exp = 1800; // default 30 mins
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String jwt = authHeader.substring(7);
                    String[] parts = jwt.split("\\.");
                    if (parts.length >= 2) {
                        String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                        com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(payload);
                        if (jsonNode.has("jti")) {
                            jti = jsonNode.get("jti").asText();
                        }
                        if (jsonNode.has("exp")) {
                            long expTime = jsonNode.get("exp").asLong();
                            exp = expTime - (System.currentTimeMillis() / 1000);
                            if (exp < 0) exp = 1800;
                        }
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
            
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("id", jti);
            sessionData.put("userId", userId);
            sessionData.put("username", localUser.getUsername());
            sessionData.put("email", localUser.getEmail());
            sessionData.put("ipAddress", request.getRemoteAddr());
            sessionData.put("userAgent", request.getHeader("User-Agent"));
            sessionData.put("isActive", true);
            sessionData.put("issuedAt", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            sessionData.put("expiresAt", ZonedDateTime.now().plusSeconds(exp).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            
            redisTemplate.opsForValue().set("active_session:" + jti, objectMapper.writeValueAsString(sessionData), exp, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, String> response = new HashMap<>();
        response.put("username", localUser.getUsername());
        response.put("userId", userId);
        response.put("roles", rolesString);
        response.put("permissions", permissionsString);
        
        return ResponseEntity.ok(response);
    }
}
