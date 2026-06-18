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
    private boolean isBlocked;

    @Builder
    public User(String id, String username, String password, String email, Set<Role> roles, Set<Permission> permissions, boolean isBlocked) {
        this.id = (id != null && !id.trim().isEmpty()) ? id : java.util.UUID.randomUUID().toString().replace("-", "");
        this.username = username;
        this.password = password;
        this.email = email;
        this.roles = roles != null ? roles : new HashSet<>();
        this.permissions = permissions != null ? permissions : new HashSet<>();
        this.isBlocked = isBlocked;
    }

    public void updateRoleAndPermissions(Set<Role> roles, Set<Permission> permissions) {
        this.roles = roles != null ? roles : new HashSet<>();
        this.permissions = permissions != null ? permissions : new HashSet<>();
    }

    public void updateStatus(boolean isBlocked) {
        this.isBlocked = isBlocked;
    }
}
