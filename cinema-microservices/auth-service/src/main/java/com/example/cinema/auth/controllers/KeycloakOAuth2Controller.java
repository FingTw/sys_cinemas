package com.example.cinema.auth.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.cinema.auth.entities.User;
import com.example.cinema.auth.repositories.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class KeycloakOAuth2Controller {

    private static final Logger log = LoggerFactory.getLogger(KeycloakOAuth2Controller.class);

    @Value("${keycloak.auth-uri}")
    private String authUri;

    @Value("${keycloak.token-uri}")
    private String tokenUri;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.frontend-redirect-url}")
    private String frontendRedirectUrl;

    @Value("${keycloak.backend-callback-url}")
    private String backendCallbackUrl;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 1. FE gọi GET /api/v1/auth/login -> BE redirect về Keycloak form
     */
    @GetMapping("/login")
    public void login(@RequestParam(value = "prompt", required = false) String prompt, HttpServletResponse response)
            throws IOException {
        String state = UUID.randomUUID().toString(); // Security state
        String keycloakAuthUrl = String.format(
                "%s?response_type=code&client_id=%s&redirect_uri=%s&state=%s&scope=openid profile email",
                authUri, clientId, backendCallbackUrl, state);
        if (prompt != null && !prompt.isEmpty()) {
            keycloakAuthUrl += "&prompt=" + prompt;
        }
        log.info("Redirecting to Keycloak for login: {}", keycloakAuthUrl);
        response.sendRedirect(keycloakAuthUrl);
    }

    /**
     * 2. Keycloak redirect về GET /api/v1/auth/callback sau khi login thành công
     */
    @GetMapping("/callback")
    public void callback(@RequestParam("code") String code,
            @RequestParam("state") String state,
            HttpServletResponse response) throws IOException {
        log.info("Received callback from Keycloak with code");

        // Gọi Keycloak Token API để lấy token
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("code", code);
        body.add("redirect_uri", backendCallbackUrl);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUri, request, Map.class);

            if (tokenResponse.getStatusCode().is2xxSuccessful() && tokenResponse.getBody() != null) {
                Map<String, Object> responseBody = tokenResponse.getBody();
                String accessToken = (String) responseBody.get("access_token");
                String refreshToken = (String) responseBody.get("refresh_token");

                // Thời gian sống của cookie = expire của token
                int expiresIn = (Integer) responseBody.getOrDefault("expires_in", 3600);
                int refreshExpiresIn = (Integer) responseBody.getOrDefault("refresh_expires_in", 86400);

                // Đồng bộ người dùng vào cơ sở dữ liệu nội bộ (nếu chưa có)
                try {
                    String[] chunks = accessToken.split("\\.");
                    if (chunks.length > 1) {
                        String payload = new String(java.util.Base64.getUrlDecoder().decode(chunks[1]));
                        JsonNode claims = objectMapper.readTree(payload);
                        String sub = claims.has("sub") ? claims.get("sub").asText() : null;
                        if (sub != null) {
                            userRepository.findBySsoSubject(sub).orElseGet(() -> {
                                String preferredUsername = claims.has("preferred_username")
                                        ? claims.get("preferred_username").asText()
                                        : sub;
                                String email = claims.has("email") ? claims.get("email").asText()
                                        : preferredUsername + "@sso.local";

                                User newUser = User.builder()
                                        .id(UUID.randomUUID().toString())
                                        .username(preferredUsername)
                                        .email(email)
                                        .ssoSubject(sub)
                                        .isBlocked(false)
                                        .build();
                                log.info("Đồng bộ User SSO mới vào database nội bộ: {}", preferredUsername);
                                return userRepository.save(newUser);
                            });
                        }
                    }
                } catch (Exception ex) {
                    log.warn("Lỗi đồng bộ User SSO vào database: {}", ex.getMessage());
                }

                // Set Cookie cho Access Token
                Cookie accessCookie = new Cookie("ACCESS_TOKEN", accessToken);
                accessCookie.setHttpOnly(true);
                accessCookie.setSecure(false); // set true nếu chạy HTTPS
                accessCookie.setPath("/");
                accessCookie.setMaxAge(expiresIn);
                accessCookie.setAttribute("SameSite", "Lax");
                response.addCookie(accessCookie);

                // Set Cookie cho Refresh Token
                if (refreshToken != null) {
                    Cookie refreshCookie = new Cookie("REFRESH_TOKEN", refreshToken);
                    refreshCookie.setHttpOnly(true);
                    refreshCookie.setSecure(false);
                    refreshCookie.setPath("/api/v1/auth/refresh-token");
                    refreshCookie.setMaxAge(refreshExpiresIn);
                    refreshCookie.setAttribute("SameSite", "Lax");
                    response.addCookie(refreshCookie);
                }

                // Redirect về Frontend Homepage
                log.info("Successfully fetched tokens. Redirecting to frontend: {}", frontendRedirectUrl);
                response.sendRedirect(frontendRedirectUrl);
            } else {
                log.error("Failed to get token from Keycloak. Status: {}", tokenResponse.getStatusCode());
                response.sendRedirect(frontendRedirectUrl + "?error=auth_failed");
            }
        } catch (Exception e) {
            log.error("Error during token exchange: {}", e.getMessage(), e);
            response.sendRedirect(frontendRedirectUrl + "?error=server_error");
        }
    }
}
