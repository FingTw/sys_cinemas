package com.example.cinema.application.usecases;

import java.util.List;
import java.util.stream.Collectors;

import com.example.cinema.application.exceptions.ClientException;
import com.example.cinema.application.exceptions.ServerException;
import com.example.cinema.application.ports.out.TokenBlacklistPort;
import org.springframework.stereotype.Service;

import com.example.cinema.application.dto.AdminUserDTO;
import com.example.cinema.domain.entities.User;
import com.example.cinema.application.ports.out.UserRepositoryPort;
import com.example.cinema.application.ports.out.RoleRepositoryPort;
import com.example.cinema.application.ports.out.PermissionRepositoryPort;
import com.example.cinema.application.ports.out.TokenCachePort;
import com.example.cinema.infrastructure.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collections;

@Service
public class AdminUserService {

    private static final Logger log = LoggerFactory.getLogger(AdminUserService.class);

    private final UserRepositoryPort userRepositoryPort;
    private final TokenBlacklistPort tokenBlacklistPort;
    private final JwtTokenProvider jwtTokenProvider;
    private final RoleRepositoryPort roleRepositoryPort;
    private final PermissionRepositoryPort permissionRepositoryPort;
    private final TokenCachePort tokenCachePort;

    public AdminUserService(UserRepositoryPort userRepositoryPort, TokenBlacklistPort tokenBlacklistPort, 
            JwtTokenProvider jwtTokenProvider, RoleRepositoryPort roleRepositoryPort, 
            PermissionRepositoryPort permissionRepositoryPort, TokenCachePort tokenCachePort) {
        this.userRepositoryPort = userRepositoryPort;
        this.tokenBlacklistPort = tokenBlacklistPort;
        this.jwtTokenProvider = jwtTokenProvider;
        this.roleRepositoryPort = roleRepositoryPort;
        this.permissionRepositoryPort = permissionRepositoryPort;
        this.tokenCachePort = tokenCachePort;
    }

    public List<AdminUserDTO> getAllUsers() {
        log.info("Fetching all users from database");
        try {
            return userRepositoryPort.findAll().stream().map(user -> {
                boolean isOnline = false;
                if (user.getActiveToken() != null && !user.getActiveToken().isEmpty()) {
                    // Trạng thái Online ưu tiên kiểm tra Cache (Redis). 
                    // Nếu Redis down hoặc Cache bị thiếu nhưng Token vẫn còn hạn (JWT validate ok) 
                    // và không bị Blacklist thì vẫn coi là Online.
                    boolean isValidInCache = tokenCachePort.isValidToken(user.getActiveToken());
                    boolean isValidJwt = jwtTokenProvider.validateToken(user.getActiveToken());
                    boolean isBlacklisted = tokenBlacklistPort.isBlacklisted(user.getActiveToken());
                    
                    isOnline = (isValidInCache || isValidJwt) && !isBlacklisted;
                }
                
                return AdminUserDTO.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .roles(user.getRoles().stream()
                            .map(com.example.cinema.domain.entities.Role::getName)
                            .collect(Collectors.joining(",")))
                    .permissions(user.getPermissions().stream()
                            .map(com.example.cinema.domain.entities.Permission::getName)
                            .collect(Collectors.joining(",")))
                    .isBlocked(user.isBlocked())
                    .isOnline(isOnline)
                    .build();
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Database error while fetching user list: {}", e.getMessage(), e);
            throw new ServerException("System error while accessing database: " + e.getMessage(), e);
        }
    }

    public List<com.example.cinema.domain.entities.Permission> getAllPermissions() {
        log.info("Fetching all available permissions");
        try {
            return permissionRepositoryPort.findAll();
        } catch (Exception e) {
            log.error("Failed to fetch permissions: {}", e.getMessage());
            throw new ServerException("Failed to fetch permissions");
        }
    }

    private void invalidateToken(User user) {
        if (user.getActiveToken() != null && !user.getActiveToken().isEmpty()) {
            try {
                java.util.Date expiresAt = jwtTokenProvider.getExpirationDateFromToken(user.getActiveToken());
                tokenBlacklistPort.blacklistToken(user.getActiveToken(), expiresAt);
                tokenCachePort.removeToken(user.getActiveToken());
                log.info("Invalidated token for User [{}]", user.getUsername());
            } catch (Exception e) {
                log.warn("Failed to blacklist Token for User [{}] - Error: {}", user.getUsername(), e.getMessage());
            }
        }
        // Luôn đảm bảo xóa ActiveToken trong Object User cho dù có lỗi Blacklist hay không
        user.setActiveToken(null);
    }

    public void changeRole(String id, String rolesString) {
        log.info("Changing roles for User ID [{}] to [{}]", id, rolesString);
        try {
            User user = userRepositoryPort.findById(id).orElseThrow(() -> new ClientException("User not found"));
            
            if (rolesString == null || rolesString.trim().isEmpty()) {
                user.setRoles(Collections.emptySet());
            } else {
                java.util.Set<com.example.cinema.domain.entities.Role> newRoles = new java.util.HashSet<>();
                // Support both comma-separated and potentially array-like formats if passed as string
                String[] roles = rolesString.replaceAll("[\\[\\]\"]", "").split(",");
                for (String roleName : roles) {
                    String trimmedName = roleName.trim();
                    if (!trimmedName.isEmpty()) {
                        com.example.cinema.domain.entities.Role role = roleRepositoryPort.findByName(trimmedName)
                                .orElseThrow(() -> new ClientException("Role [" + trimmedName + "] not found"));
                        newRoles.add(role);
                    }
                }
                user.setRoles(newRoles);
            }
            
            invalidateToken(user); // Force logout to apply new roles
            userRepositoryPort.save(user);
            
            // Đồng bộ Role mới vào Redis để các chức năng check role không cần hit DB
            tokenCachePort.cacheUserRoles(user.getId(), rolesString);
            
            log.info("Roles updated successfully for user [{}]", user.getUsername());
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("Database error while changing roles for User [{}]: {}", id, e.getMessage(), e);
            throw new ServerException("System error while changing user roles: " + e.getMessage(), e);
        }
    }

    public void blockUser(String id) {
        log.info("Updating block status for User ID [{}]", id);
        try {
            User user = userRepositoryPort.findById(id).orElseThrow(() -> new ClientException("User not found"));
            user.setBlocked(!user.isBlocked()); 
            invalidateToken(user); 
            userRepositoryPort.save(user);
            log.info("Block status updated for user [{}]. New status: {}", user.getUsername(), user.isBlocked());
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("Database error while blocking user [{}]: {}", id, e.getMessage(), e);
            throw new ServerException("System error while blocking user: " + e.getMessage(), e);
        }
    }

    public void kickUser(String id) {
        log.info("Kicking User ID [{}] from system", id);
        try {
            User user = userRepositoryPort.findById(id).orElseThrow(() -> new ClientException("User not found"));
            invalidateToken(user); 
            userRepositoryPort.save(user);
            log.info("User [{}] has been kicked successfully", user.getUsername());
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("Database error while kicking user [{}]: {}", id, e.getMessage(), e);
            throw new ServerException("System error while kicking user: " + e.getMessage(), e);
        }
    }

    public void changePermissions(String id, List<String> permissionNames) {
        log.info("Changing direct permissions for User ID [{}] to [{}]", id, permissionNames);
        try {
            User user = userRepositoryPort.findById(id).orElseThrow(() -> new ClientException("User not found"));
            
            java.util.Set<com.example.cinema.domain.entities.Permission> newPermissions = new java.util.HashSet<>();
            if (permissionNames != null) {
                for (String permName : permissionNames) {
                    if (permName != null && !permName.trim().isEmpty()) {
                        com.example.cinema.domain.entities.Permission perm = permissionRepositoryPort.findByName(permName.trim())
                                .orElseThrow(() -> new ClientException("Permission [" + permName + "] not found"));
                        newPermissions.add(perm);
                    }
                }
            }
            
            user.setPermissions(newPermissions);
            invalidateToken(user); // Force logout to apply new permissions
            userRepositoryPort.save(user);
            
            log.info("Direct permissions updated successfully for user [{}]", user.getUsername());
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("Database error while changing permissions for User [{}]: {}", id, e.getMessage(), e);
            throw new ServerException("System error while changing user permissions: " + e.getMessage(), e);
        }
    }
}
