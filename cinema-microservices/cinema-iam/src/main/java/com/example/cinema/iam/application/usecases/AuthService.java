package com.example.cinema.iam.application.usecases;

import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.cinema.common.exception.ClientException;
import com.example.cinema.common.exception.ServerException;
import com.example.cinema.common.security.ports.AuthGatewayPort;
import com.example.cinema.common.security.ports.CryptoPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

import com.example.cinema.iam.application.dto.AuthResponse;
import com.example.cinema.iam.application.dto.RegisterRequest;
import com.example.cinema.iam.domain.repositories.UserRepository;
import com.example.cinema.iam.domain.entities.User;
import com.example.cinema.common.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.cinema.iam.application.ports.in.LoginUseCase;
import com.example.cinema.iam.domain.repositories.TokenRepository;
import com.example.cinema.iam.domain.entities.AuthToken;
import java.time.ZonedDateTime;
import java.util.UUID;

import com.example.cinema.iam.domain.repositories.RoleRepository;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
public class AuthService implements LoginUseCase {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthGatewayPort authGatewayPort;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;
    private final CryptoPort cryptoPort;
    private final TokenRepository tokenRepositoryPort;
    private final RoleRepository roleRepository;
    

    public AuthService(AuthGatewayPort authGatewayPort, JwtTokenProvider jwtTokenProvider,
            UserRepository userRepositoryPort, PasswordEncoder passwordEncoder,
            StringRedisTemplate redisTemplate,
            CryptoPort cryptoPort, TokenRepository tokenRepositoryPort, RoleRepository roleRepository) {
        this.authGatewayPort = authGatewayPort;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
        this.cryptoPort = cryptoPort;
        this.tokenRepositoryPort = tokenRepositoryPort;
        this.roleRepository = roleRepository;
        
    }

    @Override
    public AuthResponse login(String username, String encryptedPassword) {
        log.info("Processing login request for user: [{}]", username);
        String password;
        try {
            password = cryptoPort.decrypt(encryptedPassword);
        } catch (Exception e) {
            log.error("Failed to decrypt password for user [{}]: {}", username, e.getMessage());
            throw new ClientException("Invalid encrypted password format");
        }

        // 1. Verify credentials with Keycloak
        try {
            boolean isValid = authGatewayPort.verifyCredentials(username, password);
            if (!isValid) {
                throw new ClientException("Invalid username or password");
            }
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("Keycloak authentication error: {}", e.getMessage(), e);
            throw new ServerException("Authentication system error: " + e.getMessage(), e);
        }

        // 2. Fetch user from DB
        User user;
        try {
            user = userRepositoryPort.findByUsername(username)
                    .orElseThrow(() -> new ClientException("User not found in system"));
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("Database error while finding user [{}]: {}", username, e.getMessage(), e);
            throw new ServerException("System error while retrieving account info: " + e.getMessage(), e);
        }

        if (user.isBlocked()) {
            throw new ClientException("Account is blocked");
        }

        // 3. Issue Refresh Token and save to DB
        String refreshToken = "";
        try {
            refreshToken = issueTokens(user, "0.0.0.0", "cinema-client");
        } catch (Exception e) {
            log.error("CRITICAL: Failed to issue refresh token for user [{}]: {}", username, e.getMessage());
            throw new ServerException("Session management error, please try again later.");
        }

        // 4. Generate internal Access Token (JWT)
        java.util.Set<String> roleNames = user.getRoles().stream()
                .map(com.example.cinema.iam.domain.entities.Role::getName)
                .collect(Collectors.toSet());

        // Aggregate all permissions from roles + direct permissions
        java.util.Set<String> effectivePermissions = new java.util.HashSet<>();
        user.getRoles().forEach(role -> role.getPermissions().forEach(p -> effectivePermissions.add(p.getName())));
        user.getPermissions().forEach(p -> effectivePermissions.add(p.getName()));

        String accessToken = jwtTokenProvider.generateToken(user.getUsername(), roleNames, effectivePermissions,
                user.getId(), user.getTokenVersion());
        log.info("Generated internal access token successfully for user: [{}]", username);

        // Blacklist old token (if any)
        if (user.getActiveToken() != null && !user.getActiveToken().isEmpty()) {
            try {
                String oldToken = user.getActiveToken();
                java.util.Date expiresAt = jwtTokenProvider.getExpirationDateFromToken(oldToken);
                redisTemplate.opsForValue().set("blacklist:" + oldToken, "true", java.time.Duration.between(java.time.Instant.now(), expiresAt.toInstant()).toMillis(), TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.warn("Could not parse or remove old token for user [{}], skipping", username);
            }
        }

        // Update active token in user table
        try {
            user.setActiveToken(accessToken);
            userRepositoryPort.save(user);

            // CACHE the valid token in Redis for super-fast validation

            // CACHE user roles and permissions for fast authorization checks
        } catch (Exception e) {
            log.error("Database error while saving token for user [{}]: {}", username, e.getMessage());
            throw new ServerException("System error while updating login session.");
        }

        return new AuthResponse(accessToken, refreshToken);
    }

    public String issueTokens(User user, String ipAddress, String userAgent) {
        try {
            String tokenJti = UUID.randomUUID().toString();

            AuthToken token = AuthToken.builder()
                    .userId(user.getId())
                    .tokenJti(tokenJti)
                    .tokenHash(passwordEncoder.encode(tokenJti)) // Store hashed JTI
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .isActive(true)
                    .issuedAt(ZonedDateTime.now())
                    .expiresAt(ZonedDateTime.now().plusDays(30))
                    .build();

            tokenRepositoryPort.save(token);
            log.info("Saved Refresh Token [JTI: {}] to database for user [{}]", tokenJti, user.getUsername());
            return tokenJti;
        } catch (Exception e) {
            log.error("Failed to save auth token to DB: {}", e.getMessage(), e);
            throw new ServerException("Failed to save auth token", e);
        }
    }

    public void register(RegisterRequest request) {
        log.info("Processing registration for user: [{}]", request.getUsername());
        try {
            if (userRepositoryPort.existsByUsername(request.getUsername())) {
                throw new ClientException("Username already exists");
            }

            String plainPassword = cryptoPort.decrypt(request.getPassword());

            // Assign default USER role
            com.example.cinema.iam.domain.entities.Role userRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new ServerException("Default USER role not found in database"));

            User newUser = User.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(plainPassword + request.getUsername().toLowerCase()))
                    .roles(Collections.singleton(userRole))
                    .build();

            userRepositoryPort.save(newUser);
            log.info("User [{}] registered successfully", request.getUsername());
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("System error while registering user [{}]: {}", request.getUsername(), e.getMessage(), e);
            throw new ServerException("System error while creating account: " + e.getMessage(), e);
        }
    }

    public AuthResponse refreshToken(String refreshTokenJti) {
        log.info("Processing refresh token request for JTI: [{}]", refreshTokenJti);

        AuthToken token = tokenRepositoryPort.findByJti(refreshTokenJti)
                .orElseThrow(() -> new ClientException("Invalid refresh token"));

        if (!token.isActive() || token.getExpiresAt().isBefore(ZonedDateTime.now())) {
            // DETECT REUSE / ATTACK
            log.warn("DETECTED REUSE OF REFRESH TOKEN! Revoking all sessions for user [{}]", token.getUserId());
            tokenRepositoryPort.revokeAllByUserId(token.getUserId());
            throw new ClientException("Refresh token has been reused or expired. Security alert triggered.");
        }

        // 1. Revoke current refresh token (Rotation)
        tokenRepositoryPort.revokeTokenByJti(refreshTokenJti);

        // 2. Fetch user
        User user = userRepositoryPort.findById(token.getUserId())
                .orElseThrow(() -> new ServerException("User not found during token refresh"));

        if (user.isBlocked()) {
            throw new ClientException("Account is blocked");
        }

        // 3. Issue NEW Refresh Token
        String newRefreshToken = issueTokens(user, "0.0.0.0", "cinema-client");

        // 4. Generate NEW Access Token
        java.util.Set<String> roleNames = user.getRoles().stream()
                .map(com.example.cinema.iam.domain.entities.Role::getName)
                .collect(Collectors.toSet());

        java.util.Set<String> effectivePermissions = new java.util.HashSet<>();
        user.getRoles().forEach(role -> role.getPermissions().forEach(p -> effectivePermissions.add(p.getName())));
        user.getPermissions().forEach(p -> effectivePermissions.add(p.getName()));

        String newAccessToken = jwtTokenProvider.generateToken(user.getUsername(), roleNames, effectivePermissions,
                user.getId(), user.getTokenVersion());

        // Update active token in user table and CACHE
        user.setActiveToken(newAccessToken);
        userRepositoryPort.save(user);

        return new AuthResponse(newAccessToken, newRefreshToken);
    }

    public void logout(String token) {
        log.info("Processing logout for token");
        try {
            // 1. Luôn xóa khỏi Cache Valid trước tiên

            // 2. Thử Blacklist nếu token hợp lệ (còn hạn)
            try {
                java.util.Date expiresAt = jwtTokenProvider.getExpirationDateFromToken(token);
                redisTemplate.opsForValue().set("blacklist:" + token, "true", java.time.Duration.between(java.time.Instant.now(), expiresAt.toInstant()).toMillis(), TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.warn("Could not determine expiration for token during logout, skipping blacklist. Error: {}",
                        e.getMessage());
            }

            // 3. Tìm user dựa trên token và xóa ActiveToken
            try {
                String username = jwtTokenProvider.getUsernameFromToken(token);
                userRepositoryPort.findByUsername(username).ifPresent(user -> {
                    if (token.equals(user.getActiveToken())) {
                        user.setActiveToken(null);
                        userRepositoryPort.save(user);
                        log.info("Cleared activeToken for user [{}] in database", username);
                    }
                });
            } catch (Exception e) {
                log.warn("Could not identify user from token during logout to clear activeToken. Error: {}",
                        e.getMessage());
                // Fallback: Nếu không lấy được username, ta vẫn đã remove khỏi cache ở bước 1,
                // nên user sẽ hiện Offline trong dashboard.
            }

            log.info("Logout sequence completed");
        } catch (Exception e) {
            log.error("Critical logout error: {}", e.getMessage(), e);
            throw new ServerException("System error during logout: " + e.getMessage(), e);
        }
    }
}