package com.example.cinema.infrastructure.database.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

// File nay chuyen dung de mapping voi PostgreSQL
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "users", schema = "auth")
public class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", schema = "auth", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<RoleJpaEntity> roles = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_permissions",
        schema = "auth",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<PermissionJpaEntity> permissions = new HashSet<>();

    @Column(length = 1000)
    private String activeToken;

    @Column(nullable = false)
    private boolean isBlocked = false;

    @Column(nullable = false)
    private Long tokenVersion = 1L;

    public UserJpaEntity() {
    }

    public UserJpaEntity(String id, String username, String password, String email, Set<RoleJpaEntity> roles,
            Set<PermissionJpaEntity> permissions, String activeToken, boolean isBlocked, Long tokenVersion) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.roles = roles != null ? roles : new HashSet<>();
        this.permissions = permissions != null ? permissions : new HashSet<>();
        this.activeToken = activeToken;
        this.isBlocked = isBlocked;
        this.tokenVersion = tokenVersion != null ? tokenVersion : 1L;
    }

    // Builder manual
    public static UserJpaEntityBuilder builder() {
        return new UserJpaEntityBuilder();
    }

    public static class UserJpaEntityBuilder {
        private String id;
        private String username;
        private String password;
        private String email;
        private Set<RoleJpaEntity> roles = new HashSet<>();
        private Set<PermissionJpaEntity> permissions = new HashSet<>();
        private String activeToken;
        private boolean isBlocked = false;
        private Long tokenVersion = 1L;

        public UserJpaEntityBuilder id(String id) {
            this.id = id;
            return this;
        }

        public UserJpaEntityBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UserJpaEntityBuilder password(String password) {
            this.password = password;
            return this;
        }

        public UserJpaEntityBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserJpaEntityBuilder roles(Set<RoleJpaEntity> roles) {
            this.roles = roles;
            return this;
        }

        public UserJpaEntityBuilder permissions(Set<PermissionJpaEntity> permissions) {
            this.permissions = permissions;
            return this;
        }

        public UserJpaEntityBuilder activeToken(String activeToken) {
            this.activeToken = activeToken;
            return this;
        }

        public UserJpaEntityBuilder isBlocked(boolean isBlocked) {
            this.isBlocked = isBlocked;
            return this;
        }

        public UserJpaEntityBuilder tokenVersion(Long tokenVersion) {
            this.tokenVersion = tokenVersion;
            return this;
        }

        public UserJpaEntity build() {
            return new UserJpaEntity(id, username, password, email, roles, permissions, activeToken, isBlocked, tokenVersion);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<RoleJpaEntity> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleJpaEntity> roles) {
        this.roles = roles;
    }

    public Set<PermissionJpaEntity> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<PermissionJpaEntity> permissions) {
        this.permissions = permissions;
    }

    public String getActiveToken() {
        return activeToken;
    }

    public void setActiveToken(String activeToken) {
        this.activeToken = activeToken;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    public Long getTokenVersion() {
        return tokenVersion;
    }

    public void setTokenVersion(Long tokenVersion) {
        this.tokenVersion = tokenVersion;
    }
}
