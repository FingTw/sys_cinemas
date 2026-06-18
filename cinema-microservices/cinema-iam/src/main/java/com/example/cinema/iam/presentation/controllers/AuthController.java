package com.example.cinema.iam.presentation.controllers;

import com.example.cinema.iam.application.dto.AuthRequest;
import com.example.cinema.iam.application.dto.AuthResponse;
import com.example.cinema.iam.application.dto.RegisterRequest;
import com.example.cinema.iam.application.ports.in.AuthServicePort;
import com.example.cinema.common.security.ports.CryptoPort;
import com.example.cinema.iam.application.ports.in.PasswordPolicyUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AuthController - Điểm tiếp nhận các yêu cầu xác thực.
 * Không cần try-catch thủ công vì đã có GlobalExceptionHandler xử lý tập trung.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthServicePort authService;
    private final CryptoPort cryptoPort;
    private final PasswordPolicyUseCase passwordPolicyUseCase;
    private final com.example.cinema.common.security.JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthServicePort authService, CryptoPort cryptoPort, PasswordPolicyUseCase passwordPolicyUseCase, com.example.cinema.common.security.JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.cryptoPort = cryptoPort;
        this.passwordPolicyUseCase = passwordPolicyUseCase;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @GetMapping("/password-policy")
    public ResponseEntity<?> getPasswordPolicy() {
        return ResponseEntity.ok(passwordPolicyUseCase.getPolicy());
    }

    @GetMapping("/public-key")
    public ResponseEntity<Map<String, String>> getPublicKey() {
        return ResponseEntity.ok(Map.of("publicKey", cryptoPort.getPublicKeyPem()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request, jakarta.servlet.http.HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        AuthResponse response = authService.login(request.getUsername(), request.getPassword(), ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    /**
     * [SSO] Nhan Keycloak Access/ID Token va Refresh Token truc tiep tu Frontend.
     * Iam se validate token offline bang JWKS, thuc hien JIT provisioning/account linking,
     * va tra ve Local JWT.
     *
     * <p>Body yeu cau (JSON):
     * <pre>
     * {
     *   "keycloakToken": "eyJhbGciOi...",
     *   "keycloakRefreshToken": "eyJhbGciOi..."
     * }
     * </pre>
     */
    @PostMapping("/sso/token")
    public ResponseEntity<AuthResponse> ssoToken(
            @RequestBody Map<String, String> request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        String keycloakToken = request.get("keycloakToken");
        String keycloakRefreshToken = request.get("keycloakRefreshToken");
        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        AuthResponse response = authService.loginWithSso(keycloakToken, keycloakRefreshToken, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody Map<String, String> request, jakarta.servlet.http.HttpServletRequest httpRequest) {
        String refreshToken = request.get("refreshToken");
        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        AuthResponse response = authService.refreshToken(refreshToken, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    private String getClientIpAddress(jakarta.servlet.http.HttpServletRequest request) {
        String[] headers = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication, HttpServletRequest request) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        
        String rolesString = "";
        String permissionsString = "";
        
        if (authentication.getAuthorities() != null) {
            rolesString = authentication.getAuthorities().stream()
                    .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                    .filter(role -> role.startsWith("ROLE_"))
                    .map(role -> role.substring(5))
                    .collect(Collectors.joining(","));
                    
            permissionsString = authentication.getAuthorities().stream()
                    .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                    .filter(role -> !role.startsWith("ROLE_"))
                    .collect(Collectors.joining(","));
        }
                
        String userId = "";
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                userId = jwtTokenProvider.getUserIdFromToken(token);
            } catch (Exception e) {
                // Ignore
            }
        }

        return ResponseEntity.ok(Map.of(
            "username", authentication.getName(),
            "roles", rolesString,
            "permissions", permissionsString,
            "userId", userId
        ));
    }

    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@RequestParam String username) {
        boolean exists = authService.checkUsername(username);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        boolean exists = authService.checkEmail(email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(Map.of("message", "Registration successful"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            HttpServletRequest request,
            @RequestParam(value = "global", required = false, defaultValue = "false") boolean global) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (global) {
                authService.logoutAll(token);
            } else {
                authService.logout(token);
            }
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
