package com.example.cinema.presentation.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.cinema.application.ports.out.CryptoPort;
import org.springframework.web.bind.annotation.RestController;

import com.example.cinema.application.dto.AuthRequest;
import com.example.cinema.application.dto.AuthResponse;
import com.example.cinema.application.dto.RegisterRequest;
import com.example.cinema.application.usecases.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final CryptoPort cryptoPort;

    public AuthController(AuthService authService, CryptoPort cryptoPort) {
        this.authService = authService;
        this.cryptoPort = cryptoPort;
    }

    @GetMapping("/public-key")
    public ResponseEntity<Map<String, String>> getPublicKey() {
        log.info("Endpoint [/public-key] called to retrieve RSA Public Key");
        return ResponseEntity.ok(Map.of("publicKey", cryptoPort.getPublicKeyPem()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        log.info("Endpoint [/login] called for Username: [{}]", request.getUsername());
        try {
            AuthResponse response = authService.login(request.getUsername(), request.getPassword());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed for user [{}]: {}", request.getUsername(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        log.info("Endpoint [/refresh-token] called");
        try {
            AuthResponse response = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(401).body(null);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(org.springframework.security.core.Authentication authentication) {
        log.info("Endpoint [/me] called to verify session");
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        return ResponseEntity.ok(Map.of(
            "username", authentication.getName(),
            "roles", authentication.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .collect(java.util.stream.Collectors.toList())
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        log.info("Endpoint [/register] called for Username: [{}]", request.getUsername());
        try {
            authService.register(request);
            return ResponseEntity.ok(Map.of("message", "Registration successful"));
        } catch (Exception e) {
            log.error("Registration failed for user [{}]: {}", request.getUsername(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(jakarta.servlet.http.HttpServletRequest request) {
        log.info("Endpoint [/logout] called");
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                authService.logout(token);
            }
            return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage());
            throw e;
        }
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception in AuthController: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
