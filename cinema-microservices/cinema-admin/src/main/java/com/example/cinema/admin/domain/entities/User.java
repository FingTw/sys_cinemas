package com.example.cinema.admin.domain.entities;

import java.util.Set;
import java.util.HashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
public class User {

    private String id;
    private String username;
    private String password;
    private String email;
    private Set<Role> roles = new HashSet<>();
    private Set<Permission> permissions = new HashSet<>();
    private String activeToken;
    private boolean isBlocked;
    private Long tokenVersion = 1L;

    @Builder
    public User(String id, String username, String password, String email, Set<Role> roles, Set<Permission> permissions, String activeToken, boolean isBlocked, Long tokenVersion) {
        this.id = (id != null && !id.trim().isEmpty()) ? id : java.util.UUID.randomUUID().toString().replace("-", "");
        this.username = username;
        this.password = password;
        this.email = email;
        this.roles = roles != null ? roles : new HashSet<>();
        this.permissions = permissions != null ? permissions : new HashSet<>();
        this.activeToken = activeToken;
        this.isBlocked = isBlocked;
        this.tokenVersion = tokenVersion != null ? tokenVersion : 1L;
    }

    public void updateRoleAndPermissions(Set<Role> roles, Set<Permission> permissions) {
        this.roles = roles != null ? roles : new HashSet<>();
        this.permissions = permissions != null ? permissions : new HashSet<>();
        this.tokenVersion++; // Invalidate existing tokens
    }

    public void updateStatus(boolean isBlocked) {
        this.isBlocked = isBlocked;
        if (isBlocked) {
            this.activeToken = null; // Kick user out if blocked
        }
    }

    public void clearActiveToken() {
        this.activeToken = null;
    }
}
