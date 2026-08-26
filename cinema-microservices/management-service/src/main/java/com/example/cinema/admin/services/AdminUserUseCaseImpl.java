package com.example.cinema.admin.services;

import com.example.cinema.admin.dto.AdminUserDTO;
import com.example.cinema.admin.dto.RoleDTO;
import com.example.cinema.admin.services.AdminUserUseCase;
import com.example.cinema.admin.entities.*;
import com.example.cinema.admin.repositories.*;
import com.example.cinema.common.exception.ClientException;
import com.example.cinema.common.exception.ServerException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import com.example.cinema.admin.services.CachePort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserUseCaseImpl implements AdminUserUseCase {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final CorsConfigRepository corsConfigRepository;
    private final PasswordPolicyRepository passwordPolicyRepository;
    private final SystemSecurityConfigRepository systemSecurityConfigRepository;
    private final CinemaRepository cinemaRepository;
    private final CachePort cachePort;
    private final ModelMapper modelMapper;

    @Value("${GATEWAY_URL}")
    private String gatewayUrl;

    @Value("${APP_SECURITY_INTERNAL_API_KEY}")
    private String internalApiKey;

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserDTO> getAllUsers() {
        log.info("Fetching all users from database for admin");
        try {
            return userRepository.findAll().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Database error while fetching user list: {}", e.getMessage(), e);
            throw new ServerException("System error while accessing database: " + e.getMessage(), e);
        }
    }

    private AdminUserDTO convertToDTO(User user) {
        boolean isOnline = cachePort.get("online:" + user.getId()) != null;

        AdminUserDTO dto = modelMapper.map(user, AdminUserDTO.class);
        dto.setRoles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.joining(",")));
                
        Set<String> effectivePermissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toSet());
        dto.setPermissions(String.join(",", effectivePermissions));
        dto.setOnline(isOnline);
        
        if (user.getCinemaId() != null && !user.getCinemaId().isEmpty()) {
            dto.setCinemaId(user.getCinemaId());
            cinemaRepository.findById(user.getCinemaId())
                .ifPresent(cinema -> dto.setCinemaName(cinema.getName()));
        }
        
        return dto;
    }

    private void invalidateToken(User user) {
        // Tích hợp Keycloak Admin API để logout user nếu cần.
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.example.cinema.admin.dto.AuthTokenDTO> getAllSessions() {
        Set<String> keys = cachePort.keys("active_session:*");
        List<com.example.cinema.admin.dto.AuthTokenDTO> sessions = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        
        for (String key : keys) {
            String data = cachePort.get(key);
            if (data != null) {
                try {
                    com.example.cinema.admin.dto.AuthTokenDTO dto = mapper.readValue(data, com.example.cinema.admin.dto.AuthTokenDTO.class);
                    sessions.add(dto);
                } catch (Exception e) {
                    log.error("Failed to parse session data for key {}: {}", key, e.getMessage());
                }
            }
        }
        return sessions;
    }

    @Override
    @Transactional
    public void revokeSession(UUID tokenId) {
        String key = "active_session:" + tokenId.toString();
        String data = cachePort.get(key);
        if (data != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(data);
                if (node.has("userId")) {
                    String userId = node.get("userId").asText();
                    cachePort.delete("user_perms:" + userId);
                    cachePort.delete("online:" + userId);
                }
            } catch (Exception e) {
                log.error("Failed to parse session data during revocation: {}", e.getMessage());
            }
        }
        cachePort.delete(key);
        log.info("Revoked session {}", tokenId);
    }

    @Override
    @Transactional
    public void changeRole(String id, String rolesString) {
        log.info("Changing roles for User ID [{}] to [{}]", id, rolesString);
        try {
            User user = userRepository.findById(id).orElseThrow(() -> new ClientException("User not found"));
            
            if (rolesString == null || rolesString.trim().isEmpty()) {
                user.updateRoleAndPermissions(Collections.emptySet(), user.getPermissions());
            } else {
                Set<Role> newRoles = new HashSet<>();
                String[] roles = rolesString.replaceAll("[\\[\\]\"]", "").split(",");
                for (String roleName : roles) {
                    String trimmedName = roleName.trim();
                    if (!trimmedName.isEmpty()) {
                        Role role = roleRepository.findByName(trimmedName)
                                .orElseThrow(() -> new ClientException("Role [" + trimmedName + "] not found"));
                        newRoles.add(role);
                    }
                }
                user.updateRoleAndPermissions(newRoles, user.getPermissions());
            }
            
            invalidateToken(user); 
            userRepository.save(user);
            log.info("Roles updated successfully for user [{}]", user.getUsername());
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("Database error while changing roles for User [{}]: {}", id, e.getMessage(), e);
            throw new ServerException("System error while changing user roles: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void blockUser(String id) {
        log.info("Updating block status for User ID [{}]", id);
        try {
            User user = userRepository.findById(id).orElseThrow(() -> new ClientException("User not found"));
            user.updateStatus(!user.isBlocked()); 
            invalidateToken(user); 
            userRepository.save(user);
            log.info("Block status updated for user [{}]. New status: {}", user.getUsername(), user.isBlocked());
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("Database error while blocking user [{}]: {}", id, e.getMessage(), e);
            throw new ServerException("System error while blocking user: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void kickUser(String id) {
        log.info("Kicking User ID [{}] from system", id);
        try {
            User user = userRepository.findById(id).orElseThrow(() -> new ClientException("User not found"));
            invalidateToken(user); 
            userRepository.save(user);
            log.info("User [{}] has been kicked successfully", user.getUsername());
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("Database error while kicking user [{}]: {}", id, e.getMessage(), e);
            throw new ServerException("System error while kicking user: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void assignWorkplace(String userId, String cinemaId) {
        log.info("Assigning workplace Cinema ID [{}] to User ID [{}]", cinemaId, userId);
        try {
            User user = userRepository.findById(userId).orElseThrow(() -> new ClientException("User not found"));
            if (cinemaId != null && !cinemaId.trim().isEmpty()) {
                // Verify cinema exists
                cinemaRepository.findById(cinemaId).orElseThrow(() -> new ClientException("Cinema not found"));
                userRepository.updateWorkplace(userId, cinemaId);
            } else {
                userRepository.updateWorkplace(userId, null);
            }
            
            invalidateToken(user);
            log.info("User [{}] workplace updated successfully", user.getUsername());
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("Database error while assigning workplace for User [{}]: {}", userId, e.getMessage(), e);
            throw new ServerException("System error while assigning workplace: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Permission> getAllPermissions() {
        try {
            return permissionRepository.findAll();
        } catch (Exception e) {
            throw new ServerException("Failed to fetch permissions: " + e.getMessage(), e);
        }
    }

    @Override
    public List<RoleDTO> getAllRoles() {
        try {
            return roleRepository.findAll().stream().map(role -> {
                Set<String> perms = role.getPermissions().stream().map(Permission::getName).collect(Collectors.toSet());
                return RoleDTO.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .permissions(perms)
                        .build();
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new ServerException("Failed to fetch roles: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public RoleDTO createRole(String name, Set<String> permissions) {
        // Chuẩn hóa tên role: strip ROLE_ prefix, trim, uppercase
        String normalizedName = normalizeRoleName(name);
        log.info("Creating role [{}] (normalized from '{}') with permissions: {}", normalizedName, name, permissions);

        // Không cho tạo role trùng tên với role hệ thống
        if (PROTECTED_ROLES.contains(normalizedName)) {
            throw new ClientException("Không thể tạo vai trò trùng tên với vai trò hệ thống: " + normalizedName);
        }

        try {
            Set<Permission> domainPerms = new HashSet<>();
            List<Permission> allPerms = permissionRepository.findAll();
            for (String permName : permissions) {
                Permission p = allPerms.stream()
                        .filter(item -> item.getName().equals(permName))
                        .findFirst()
                        .orElseThrow(() -> new ClientException("Permission [" + permName + "] not found"));
                domainPerms.add(p);
            }

            Role role = Role.builder()
                    .name(normalizedName)
                    .permissions(domainPerms)
                    .build();

            Role saved = roleRepository.save(role);
            return RoleDTO.builder()
                    .id(saved.getId())
                    .name(saved.getName())
                    .permissions(saved.getPermissions().stream().map(Permission::getName).collect(Collectors.toSet()))
                    .build();
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            throw new ServerException("Failed to create role: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public RoleDTO updateRolePermissions(UUID id, Set<String> permissions) {
        log.info("Updating permissions for role ID [{}] to: {}", id, permissions);
        try {
            Role role = roleRepository.findById(id)
                    .orElseThrow(() -> new ClientException("Role not found"));

            Set<Permission> domainPerms = new HashSet<>();
            List<Permission> allPerms = permissionRepository.findAll();
            for (String permName : permissions) {
                Permission p = allPerms.stream()
                        .filter(item -> item.getName().equals(permName))
                        .findFirst()
                        .orElseThrow(() -> new ClientException("Permission [" + permName + "] not found"));
                domainPerms.add(p);
            }

            role.updatePermissions(domainPerms);
            Role saved = roleRepository.save(role);
            return RoleDTO.builder()
                    .id(saved.getId())
                    .name(saved.getName())
                    .permissions(saved.getPermissions().stream().map(Permission::getName).collect(Collectors.toSet()))
                    .build();
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            throw new ServerException("Failed to update role permissions: " + e.getMessage(), e);
        }
    }

    /**
     * Chuẩn hóa tên role: strip prefix ROLE_, trim, uppercase.
     * VD: "ROLE_MANAGER" → "MANAGER", "  admin  " → "ADMIN"
     */
    private String normalizeRoleName(String name) {
        String normalized = name.trim().toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }
        return normalized;
    }

    // Danh sách role hệ thống mặc định — không được xóa
    private static final Set<String> PROTECTED_ROLES = Set.of("ADMIN", "STAFF", "USER");

    @Override
    @Transactional
    public void deleteRole(UUID id) {
        log.info("Deleting role ID [{}]", id);
        try {
            Role role = roleRepository.findById(id)
                    .orElseThrow(() -> new ClientException("Role not found with ID: " + id));

            if (PROTECTED_ROLES.contains(normalizeRoleName(role.getName()))) {
                throw new ClientException("Không thể xóa vai trò hệ thống mặc định: " + role.getName());
            }

            roleRepository.deleteById(id);
            log.info("Role [{}] deleted successfully", role.getName());
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            throw new ServerException("Failed to delete role: " + e.getMessage(), e);
        }
    }

    @Override
    public PasswordPolicy getPasswordPolicy() {
        return passwordPolicyRepository.findById("default-policy")
                .orElseGet(() -> new PasswordPolicy("default-policy", 8, true, true, true, true, LocalDateTime.now()));
    }

    @Override
    @Transactional
    public PasswordPolicy updatePasswordPolicy(PasswordPolicy request) {
        PasswordPolicy policy = passwordPolicyRepository.findById("default-policy")
                .orElseGet(() -> new PasswordPolicy("default-policy", 8, true, true, true, true, LocalDateTime.now()));

        policy.setMinLength(request.getMinLength());
        policy.setRequireUppercase(request.isRequireUppercase());
        policy.setRequireLowercase(request.isRequireLowercase());
        policy.setRequireNumber(request.isRequireNumber());
        policy.setRequireSpecialChar(request.isRequireSpecialChar());
        policy.setUpdatedAt(LocalDateTime.now());

        passwordPolicyRepository.save(policy);
        return policy;
    }

    @Override
    public SystemSecurityConfig getSystemSecurityConfig() {
        return systemSecurityConfigRepository.findById("default-security")
                .orElseGet(() -> SystemSecurityConfig.builder()
                        .id("default-security")
                        .clientKey("my-secret-dev-api-key")
                        .gatewayProtectedPaths("/api/v1/auth/login,/api/v1/auth/register,/api/v1/auth/public-key,/api/v1/auth/refresh-token,/api/v1/public")
                        .serviceBypassPaths("/actuator,/v3/api-docs,/swagger-ui,/api/v1/auth/public-key,/api/v1/movies,/api/v1/showtimes,/api/v1/rooms,/api/v1/facilities,/api/v1/vnpay")
                        .updatedAt(LocalDateTime.now())
                        .build());
    }

    @Override
    @Transactional
    public SystemSecurityConfig updateSystemSecurityConfig(SystemSecurityConfig request) {
        SystemSecurityConfig config = systemSecurityConfigRepository.findById("default-security")
                .orElseGet(() -> SystemSecurityConfig.builder()
                        .id("default-security")
                        .build());

        config.setClientKey(request.getClientKey());
        config.setGatewayProtectedPaths(request.getGatewayProtectedPaths());
        config.setServiceBypassPaths(request.getServiceBypassPaths());
        config.setUpdatedAt(LocalDateTime.now());

        systemSecurityConfigRepository.save(config);

        // Sync to Redis
        try {
            cachePort.set("security:client-key", config.getClientKey());
            cachePort.set("security:bypass-paths", config.getServiceBypassPaths());
        } catch (Exception e) {
            log.warn("Could not sync security configuration to Redis: {}", e.getMessage());
        }

        // Notify Gateway
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Internal-Api-Key", internalApiKey);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            restTemplate.exchange(gatewayUrl + "/internal/gateway/refresh-security", HttpMethod.POST, entity, Void.class);
            log.info("Notified API Gateway to refresh security configuration.");
        } catch (Exception e) {
            log.warn("Could not notify gateway to refresh security: {}", e.getMessage());
        }

        return config;
    }

    @Override
    public CorsConfig getCorsConfig() {
        return corsConfigRepository.findById("default-cors")
                .orElseGet(() -> new CorsConfig("default-cors", "*", "GET,POST,PUT,DELETE,OPTIONS", "*", LocalDateTime.now()));
    }

    @Override
    @Transactional
    public CorsConfig updateCorsConfig(CorsConfig request) {
        CorsConfig config = corsConfigRepository.findById("default-cors")
                .orElseGet(() -> new CorsConfig("default-cors", "*", "GET,POST,PUT,DELETE,OPTIONS", "*", LocalDateTime.now()));

        config.setAllowedOrigins(request.getAllowedOrigins());
        config.setAllowedMethods(request.getAllowedMethods());
        config.setAllowedHeaders(request.getAllowedHeaders());
        config.setUpdatedAt(LocalDateTime.now());

        corsConfigRepository.save(config);

        // Notify API Gateway
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Internal-Api-Key", internalApiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            restTemplate.exchange(gatewayUrl + "/internal/gateway/refresh-cors", HttpMethod.POST, entity, Void.class);
            log.info("Notified API Gateway to refresh CORS configuration.");
        } catch (Exception e) {
            log.warn("Could not notify gateway to refresh CORS: {}", e.getMessage());
        }

        return config;
    }
}
