package com.example.cinema.iam.application.usecases;

import com.example.cinema.common.security.JwtTokenProvider;
import com.example.cinema.common.security.ports.AuthGatewayPort;
import com.example.cinema.common.security.ports.CryptoPort;
import com.example.cinema.common.security.ports.SsoUserInfo;
import com.example.cinema.iam.application.dto.AuthResponse;
import com.example.cinema.iam.application.dto.RegisterRequest;
import com.example.cinema.iam.application.ports.in.AuthServicePort;
import com.example.cinema.iam.domain.entities.AuthToken;
import com.example.cinema.iam.domain.entities.User;
import com.example.cinema.iam.domain.repositories.SsoRoleMappingRepository;
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
    private final SsoRoleMappingRepository ssoRoleMappingRepository;
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

        // 2. Tim nguoi dung trong DB noi bo va kiem tra mat khau truc tiep (BCrypt)
        // Luong nay khong qua Keycloak — nhanh hon, khong phu thuoc mang noi bo.
        // Logic hash khi dang ky: encode(plainPassword + username.toLowerCase()) — giu nguyen o day.
        User user = userRepositoryPort.findByUsername(username)
                .orElseThrow(() -> IamException.authenticationFailed("Tai khoan hoac mat khau khong chinh xac"));

        if (!passwordEncoder.matches(password + username.toLowerCase(), user.getPassword())) {
            log.warn("[SECURITY] Sai mat khau cho tai khoan: [{}]", maskUsername(username));
            // Tra ve cung thong bao loi de tranh username enumeration attack
            throw IamException.authenticationFailed("Tai khoan hoac mat khau khong chinh xac");
        }

        // 3. Kiem tra trang thai tai khoan
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

        String accessToken = jwtTokenProvider.generateToken(user.getUsername(), user.getId());

        // 6. Blacklist token cũ nếu đang đăng nhập (lấy từ Redis thay vì DB)
        String prevToken = redisTemplate.opsForValue().get("valid_token:" + user.getId());
        if (prevToken != null && !prevToken.isEmpty()) {
            blacklistTokenSafely(prevToken);
        }

        // 7. Cập nhật phiên làm việc mới - Chỉ lưu Cache hỗ trợ Gateway validation nhanh, KHÔNG ghi DB
        redisTemplate.opsForValue().set("valid_token:" + user.getId(), accessToken);
        String authContext = String.join(",", roleNames) + "|" + String.join(",", effectivePermissions) + "|" + (user.getCinemaId() != null ? user.getCinemaId() : "");
        redisTemplate.opsForValue().set("user_auth:" + user.getId(), authContext);

        log.info("[AUTH] User [{}] logged in successfully", maskUsername(username));
        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .roles(String.join(",", roleNames))
                .permissions(String.join(",", effectivePermissions))
                .userId(user.getId())
                .build();
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

        String newAccessToken = jwtTokenProvider.generateToken(user.getUsername(), user.getId());

        // Không lưu access_token vào DB nữa, chỉ cập nhật Cache Redis
        redisTemplate.opsForValue().set("valid_token:" + user.getId(), newAccessToken);
        String authContext = String.join(",", roleNames) + "|" + String.join(",", effectivePermissions) + "|" + (user.getCinemaId() != null ? user.getCinemaId() : "");
        redisTemplate.opsForValue().set("user_auth:" + user.getId(), authContext);

        Set<String> refreshRoleNames = user.getRoles().stream()
                .map(com.example.cinema.iam.domain.entities.Role::getName)
                .collect(Collectors.toSet());
        return AuthResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .username(user.getUsername())
                .roles(String.join(",", refreshRoleNames))
                .permissions(String.join(",", effectivePermissions))
                .userId(user.getId())
                .build();
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

            // 3. Revoke All Refresh Tokens in DB (Sessions)
            tokenRepositoryPort.revokeAllByUserId(userId);

            log.info("[AUTH] User [{}] logged out locally", maskUsername(username));
        } catch (Exception e) {
            log.warn("[AUTH] Local logout partially failed: {}", e.getMessage());
        }
    }

    @Override
    public void logoutAll(String token) {
        try {
            String userId = jwtTokenProvider.getUserIdFromToken(token);

            // 1. Perform local logout
            logout(token);

            // 2. Backchannel logout from Keycloak
            String kcRefresh = redisTemplate.opsForValue().get("kc_refresh:" + userId);
            if (kcRefresh != null) {
                try {
                    authGatewayPort.logoutFromKeycloak(kcRefresh);
                } catch (Exception e) {
                    log.warn("[SSO] Keycloak backchannel logout failed (ignored): {}", e.getMessage());
                }
                redisTemplate.delete("kc_refresh:" + userId);
            }

            log.info("[AUTH] User [{}] logged out from Keycloak globally", userId);
        } catch (Exception e) {
            log.warn("[AUTH] Global logout partially failed: {}", e.getMessage());
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

    // --- SSO Use Case ---

    /**
     * Dang nhap bang SSO Keycloak Standalone.
     * Nhan truc tiep Keycloak JWT va Refresh Token tu Frontend, validate offline,
     * sau do dung JIT provisioning de lien ket/tao moi tai khoan trong DB
     * va phat Local JWT.
     */
    @Override
    @Transactional
    public AuthResponse loginWithSso(String keycloakJwt, String keycloakRefreshToken, String ipAddress, String userAgent) {
        log.info("[SSO] Bat dau xu ly SSO login");

        // Validate input
        if (keycloakJwt == null || keycloakJwt.trim().isEmpty()) {
            throw IamException.authenticationFailed("Keycloak token khong duoc de trong");
        }

        // Buoc 1: Validate offline Keycloak JWT bang JWKS
        SsoUserInfo ssoUser;
        try {
            ssoUser = authGatewayPort.validateKeycloakToken(keycloakJwt);
        } catch (Exception e) {
            log.error("[SSO] Validate Keycloak JWT offline that bai. Loi: {}", e.getMessage());
            throw IamException.authenticationFailed("Xac thuc SSO that bai. Token khong hop le hoac da het han.");
        }

        log.info("[SSO] Keycloak JWT xac thuc thanh cong cho user: [{}], sub: [{}].",
                maskUsername(ssoUser.username()), ssoUser.sub());

        // Buoc 2: Tim tai khoan tuong ung theo ssoSubject
        User user = userRepositoryPort.findBySsoSubject(ssoUser.sub())
                .orElseGet(() -> {
                    // Neu khong tim thay theo ssoSubject, kiem tra theo username
                    return userRepositoryPort.findByUsername(ssoUser.username())
                            .map(existingUser -> {
                                if (existingUser.getSsoSubject() == null) {
                                    log.info("[SSO] Lien ket tai khoan local [{}] voi Keycloak sub [{}]", 
                                            existingUser.getUsername(), ssoUser.sub());
                                    
                                    // Update ssoSubject va set authProvider = keycloak (Giu nguyen roles local dang co trong DB)
                                    User updatedUser = User.builder()
                                            .id(existingUser.getId())
                                            .username(existingUser.getUsername())
                                            .password(existingUser.getPassword())
                                            .email(existingUser.getEmail())
                                            .roles(existingUser.getRoles())
                                            .permissions(existingUser.getPermissions())
                                            .isBlocked(existingUser.isBlocked())
                                            .authProvider("keycloak")
                                            .ssoSubject(ssoUser.sub())
                                            .build();
                                    userRepositoryPort.save(updatedUser);
                                    return updatedUser;
                                } else {
                                    // Username conflict: trung username nhung khac sso_subject
                                    String uniqueUsername = resolveUniqueUsername(ssoUser.username());
                                    return createSsoUser(ssoUser, uniqueUsername);
                                }
                            })
                            .orElseGet(() -> createSsoUser(ssoUser, ssoUser.username()));
                });

        // Buoc 3: Kiem tra trang thai tai khoan
        if (user.isBlocked()) {
            log.warn("[SSO] Tai khoan [{}] dang bi khoa, tu choi dang nhap SSO.", maskUsername(user.getUsername()));
            throw IamException.authenticationFailed("Tai khoan da bi khoa");
        }

        // Buoc 4: Luu Keycloak refresh token vao Redis
        if (keycloakRefreshToken != null && !keycloakRefreshToken.trim().isEmpty()) {
            redisTemplate.opsForValue().set("kc_refresh:" + user.getId(),
                    keycloakRefreshToken, 30, TimeUnit.DAYS);
        }

        // Buoc 5: Phat Local JWT (y hethuong)
        String refreshToken = issueTokens(user, ipAddress, userAgent);

        Set<String> roleNames = user.getRoles().stream()
                .map(com.example.cinema.iam.domain.entities.Role::getName)
                .collect(Collectors.toSet());

        Set<String> effectivePermissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(com.example.cinema.iam.domain.entities.Permission::getName)
                .collect(Collectors.toSet());

        String accessToken = jwtTokenProvider.generateToken(user.getUsername(), user.getId());

        String prevToken = redisTemplate.opsForValue().get("valid_token:" + user.getId());
        if (prevToken != null && !prevToken.isEmpty()) {
            blacklistTokenSafely(prevToken);
        }

        // Không lưu access_token vào DB nữa, chỉ cập nhật Cache Redis

        redisTemplate.opsForValue().set("valid_token:" + user.getId(), accessToken);
        String authContext = String.join(",", roleNames) + "|" + String.join(",", effectivePermissions) + "|" + (user.getCinemaId() != null ? user.getCinemaId() : "");
        redisTemplate.opsForValue().set("user_auth:" + user.getId(), authContext);

        log.info("[SSO] User [{}] dang nhap SSO thanh cong.", maskUsername(user.getUsername()));
        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .roles(String.join(",", roleNames))
                .permissions(String.join(",", effectivePermissions))
                .userId(user.getId())
                .build();
    }

    // --- Internal Helpers ---

    private String resolveUniqueUsername(String baseUsername) {
        String username = baseUsername;
        int counter = 1;
        while (userRepositoryPort.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }
        return username;
    }

    private User createSsoUser(SsoUserInfo ssoUser, String username) {
        if (ssoUser.email() != null && !ssoUser.email().trim().isEmpty()) {
            if (userRepositoryPort.existsByEmail(ssoUser.email())) {
                throw IamException.registrationFailed("Email da ton tai trong he thong", null);
            }
        }

        Set<com.example.cinema.iam.domain.entities.Role> mappedRoles = mapKeycloakRolesToDbRoles(ssoUser.roles());

        User newUser = User.builder()
                .username(username)
                .email(ssoUser.email())
                .password(null) // SSO user khong co password
                .authProvider("keycloak")
                .ssoSubject(ssoUser.sub())
                .roles(mappedRoles)
                .build();

        userRepositoryPort.save(newUser);
        log.info("[SSO] Created new JIT provisioned user: [{}] with roles: {}", username, 
                mappedRoles.stream().map(com.example.cinema.iam.domain.entities.Role::getName).collect(Collectors.joining(",")));
        return newUser;
    }

    private Set<com.example.cinema.iam.domain.entities.Role> mapKeycloakRolesToDbRoles(Set<String> ssoRoles) {
        // 1. Quét map Keycloak Roles -> SsoRoleMapping -> Local Roles
        Set<com.example.cinema.iam.domain.entities.Role> mappedRoles = ssoRoles.stream()
                .map(roleName -> ssoRoleMappingRepository.findBySsoRoleName(roleName.toLowerCase()))
                .filter(java.util.Optional::isPresent)
                .map(mapping -> mapping.get().getLocalRole())
                .collect(Collectors.toSet());

        // 2. Fallback -> Nếu không map được role nào, cấp cho User role mặc định
        if (mappedRoles.isEmpty()) {
            log.warn("[SSO] No mapped roles found for SSO roles {}. Falling back to default USER role.", ssoRoles);
            com.example.cinema.iam.domain.entities.Role userRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> IamException.databaseError("sso.role_lookup", 
                            new RuntimeException("Default USER role missing")));
            return Collections.singleton(userRole);
        }

        return mappedRoles;
    }

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