package com.example.cinema.iam.application.usecases;

import com.example.cinema.common.security.JwtTokenProvider;
import com.example.cinema.common.security.ports.AuthGatewayPort;
import com.example.cinema.common.security.ports.CryptoPort;
import com.example.cinema.iam.application.dto.AuthResponse;
import com.example.cinema.iam.application.dto.RegisterRequest;
import com.example.cinema.iam.application.ports.in.AuthServicePort;
import com.example.cinema.iam.domain.entities.AuthToken;
import com.example.cinema.iam.domain.entities.User;
import com.example.cinema.iam.domain.repositories.RoleRepository;
import com.example.cinema.iam.domain.repositories.TokenRepository;
import com.example.cinema.iam.domain.repositories.UserRepository;
import com.example.cinema.iam.exception.IamException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AuthServiceImpl - Trien khai logic xac thuc va quan ly phien lam viec.
 * Logs entry/exit va loi duoc xu ly tu dong boi MethodLoggingAspect.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthServicePort {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;
    private final CryptoPort cryptoPort;
    private final TokenRepository tokenRepositoryPort;
    private final RoleRepository roleRepository;
    private final AuthGatewayPort authGatewayPort;
    private final com.example.cinema.iam.domain.repositories.PasswordPolicyRepository passwordPolicyRepository;

    @Override
    @Transactional
    public AuthResponse login(String username, String encryptedPassword, String ipAddress, String userAgent) {
        log.info("[AUTH] Attempting login for user: {}", maskUsername(username));
        // 0. Validate input
        if (username == null || username.trim().isEmpty()) {
            throw IamException.authenticationFailed("Ten dang nhap khong duoc de trong");
        }
        if (encryptedPassword == null || encryptedPassword.trim().isEmpty()) {
            throw IamException.authenticationFailed("Mat khau khong duoc de trong");
        }

        // 1. Giai ma mat khau tu client
        String password;
        try {
            password = cryptoPort.decrypt(encryptedPassword);
            if (password == null || password.trim().isEmpty()) {
                throw new RuntimeException("Giai ma mat khau ra ket qua trong");
            }
        } catch (Exception e) {
            log.error("[SECURITY] Decryption failed for user: [{}]. Error: {}", maskUsername(username), e.getMessage());
            throw IamException.authenticationFailed("Mat khau khong hop le (loi ma hoa)");
        }

        // 2. GOI KEYCLOAK DE KIEM TRA (SPI doc DB)
        try {
            boolean isKeycloakValid = authGatewayPort.verifyCredentials(username, password);
            if (!isKeycloakValid) {
                throw IamException.authenticationFailed("Xac thuc Keycloak that bai");
            }
        } catch (Exception e) {
            log.error("[SECURITY] Keycloak authentication failed for user: [{}]. Error: {}", maskUsername(username), e.getMessage());
            throw IamException.authenticationFailed("Tai khoan hoac mat khau khong chinh xac (Keycloak)");
        }

        // 3. Tim nguoi dung trong DB noi bo de lay Role/Permission
        User user = userRepositoryPort.findByUsername(username)
                .orElseThrow(() -> IamException.authenticationFailed("Tai khoan da xac thuc nhung khong tim thay thong tin phan quyen"));

        if (user.isBlocked()) {
            throw IamException.authenticationFailed("Tai khoan da bi khoa");
        }

        // 4. Cấp Refresh Token mới
        String refreshToken = issueTokens(user, ipAddress, userAgent);

        // 5. Tạo Access Token (JWT)
        Set<String> roleNames = user.getRoles().stream()
                .map(com.example.cinema.iam.domain.entities.Role::getName)
                .collect(Collectors.toSet());

        Set<String> effectivePermissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(com.example.cinema.iam.domain.entities.Permission::getName)
                .collect(Collectors.toSet());

        String accessToken = jwtTokenProvider.generateToken(user.getUsername(), roleNames, effectivePermissions,
                user.getId(), user.getTokenVersion());

        // 6. Blacklist token cũ nếu đang đăng nhập
        if (user.getActiveToken() != null && !user.getActiveToken().isEmpty()) {
            blacklistTokenSafely(user.getActiveToken());
        }

        // 7. Cập nhật phiên làm việc mới
        user.assignToken(accessToken);
        userRepositoryPort.save(user);

        // Lưu Cache hỗ trợ Gateway validation nhanh
        redisTemplate.opsForValue().set("valid_token:" + user.getId(), accessToken);
        String authContext = String.join(",", roleNames) + "|" + String.join(",", effectivePermissions);
        redisTemplate.opsForValue().set("user_auth:" + user.getId(), authContext);

        log.info("[AUTH] User [{}] logged in successfully", maskUsername(username));
        return new AuthResponse(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        // 0. Validate input
        if (request == null || request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw IamException.registrationFailed("Ten dang nhap khong duoc de trong", null);
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw IamException.registrationFailed("Mat khau khong duoc de trong", null);
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw IamException.registrationFailed("Email khong duoc de trong", null);
        }

        if (userRepositoryPort.existsByUsername(request.getUsername())) {
            throw IamException.duplicateUser(request.getUsername());
        }

        try {
            String plainPassword = cryptoPort.decrypt(request.getPassword());
            
            com.example.cinema.iam.domain.entities.PasswordPolicy policy = passwordPolicyRepository.findById("default-policy")
                    .orElseGet(() -> new com.example.cinema.iam.domain.entities.PasswordPolicy("default-policy", 8, true, true, true, true, java.time.LocalDateTime.now()));
            if (!policy.validate(plainPassword)) {
                throw IamException.registrationFailed("Mat khau khong dap ung yeu cau bao mat hien tai", null);
            }

            com.example.cinema.iam.domain.entities.Role userRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> IamException.databaseError("register.role_lookup", 
                            new RuntimeException("Default USER role missing")));

            User newUser = User.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(plainPassword + request.getUsername().toLowerCase()))
                    .roles(Collections.singleton(userRole))
                    .build();

            userRepositoryPort.save(newUser);
            log.info("[REGISTRATION] New user [{}] created", maskUsername(request.getUsername()));
        } catch (IamException e) {
            throw e;
        } catch (Exception e) {
            throw IamException.registrationFailed(e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshTokenJti, String ipAddress, String userAgent) {
        if (refreshTokenJti == null || refreshTokenJti.trim().isEmpty()) {
            throw IamException.authenticationFailed("Refresh token khong duoc de trong");
        }

        AuthToken token = tokenRepositoryPort.findByJti(refreshTokenJti)
                .orElseThrow(() -> IamException.authenticationFailed("Phien lam viec khong ton tai"));

        if (!token.isActive() || token.getExpiresAt().isBefore(ZonedDateTime.now())) {
            log.error("[SECURITY] REFRESH TOKEN REUSE DETECTED! User ID: [{}]", token.getUserId());
            tokenRepositoryPort.revokeAllByUserId(token.getUserId());
            throw IamException.authenticationFailed("Phat hien truy cap trai phep. Tat ca phien lam viec da bi thu hoi.");
        }

        // Rotate Refresh Token
        tokenRepositoryPort.revokeTokenByJti(refreshTokenJti);

        User user = userRepositoryPort.findById(token.getUserId())
                .orElseThrow(() -> IamException.userNotFound(token.getUserId()));

        if (user.isBlocked()) {
            throw IamException.authenticationFailed("Tai khoan da bi khoa");
        }

        String newRefreshToken = issueTokens(user, ipAddress, userAgent);

        Set<String> roleNames = user.getRoles().stream()
                .map(com.example.cinema.iam.domain.entities.Role::getName)
                .collect(Collectors.toSet());
        Set<String> effectivePermissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(com.example.cinema.iam.domain.entities.Permission::getName)
                .collect(Collectors.toSet());

        String newAccessToken = jwtTokenProvider.generateToken(user.getUsername(), roleNames, effectivePermissions,
                user.getId(), user.getTokenVersion());

        user.assignToken(newAccessToken);
        userRepositoryPort.save(user);
        redisTemplate.opsForValue().set("valid_token:" + user.getId(), newAccessToken);

        return new AuthResponse(newAccessToken, newRefreshToken);
    }

    @Override
    @Transactional
    public void logout(String token) {
        try {
            String userId = jwtTokenProvider.getUserIdFromToken(token);
            String username = jwtTokenProvider.getUsernameFromToken(token);

            // 1. Clear Cache
            redisTemplate.delete("valid_token:" + userId);
            redisTemplate.delete("user_auth:" + userId);

            // 2. Blacklist Access Token
            blacklistTokenSafely(token);

            // 3. Clear Active Token in DB
            userRepositoryPort.findByUsername(username).ifPresent(user -> {
                if (token.equals(user.getActiveToken())) {
                    user.revokeToken();
                    userRepositoryPort.save(user);
                }
            });

            // 4. Revoke All Refresh Tokens in DB (Sessions)
            tokenRepositoryPort.revokeAllByUserId(userId);

            log.info("[AUTH] User [{}] logged out and sessions revoked", maskUsername(username));
        } catch (Exception e) {
            log.warn("[AUTH] Logout partially failed: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkUsername(String username) {
        if (username == null || username.trim().isEmpty()) return false;
        return userRepositoryPort.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        return userRepositoryPort.existsByEmail(email);
    }

    // --- Internal Helpers ---

    public String issueTokens(User user, String ipAddress, String userAgent) {
        String tokenJti = UUID.randomUUID().toString();
        AuthToken token = AuthToken.builder()
                .userId(user.getId())
                .tokenJti(tokenJti)
                .tokenHash(passwordEncoder.encode(tokenJti))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .isActive(true)
                .issuedAt(ZonedDateTime.now())
                .expiresAt(ZonedDateTime.now().plusDays(30))
                .build();

        tokenRepositoryPort.save(token);
        return tokenJti;
    }

    private void blacklistTokenSafely(String token) {
        try {
            Date expiresAt = jwtTokenProvider.getExpirationDateFromToken(token);
            long ttl = expiresAt.getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                redisTemplate.opsForValue().set("blacklist:" + token, "true", ttl, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            log.warn("Failed to blacklist token: {}", e.getMessage());
        }
    }

    private String maskUsername(String username) {
        if (username == null || username.length() <= 2) return "***";
        return username.substring(0, 2) + "***";
    }
}