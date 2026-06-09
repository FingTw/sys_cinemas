package com.example.cinema.admin.application.ports.in;

import com.example.cinema.admin.application.dto.AdminUserDTO;
import com.example.cinema.admin.application.dto.RoleDTO;
import com.example.cinema.admin.domain.entities.Permission;
import com.example.cinema.admin.domain.entities.PasswordPolicy;
import com.example.cinema.admin.domain.entities.SystemSecurityConfig;
import com.example.cinema.admin.domain.entities.CorsConfig;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface AdminUserUseCase {
    List<AdminUserDTO> getAllUsers();
    
    // Session Management
    List<com.example.cinema.admin.application.dto.AuthTokenDTO> getAllSessions();
    void revokeSession(UUID tokenId);

    void changeRole(String id, String rolesString);
    void blockUser(String id);
    void kickUser(String id);
    List<Permission> getAllPermissions();

    List<RoleDTO> getAllRoles();
    RoleDTO createRole(String name, Set<String> permissions);
    RoleDTO updateRolePermissions(UUID id, Set<String> permissions);
    void deleteRole(UUID id);

    PasswordPolicy getPasswordPolicy();
    PasswordPolicy updatePasswordPolicy(PasswordPolicy request);

    SystemSecurityConfig getSystemSecurityConfig();
    SystemSecurityConfig updateSystemSecurityConfig(SystemSecurityConfig request);

    CorsConfig getCorsConfig();
    CorsConfig updateCorsConfig(CorsConfig request);
}
