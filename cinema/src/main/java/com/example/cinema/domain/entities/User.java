package com.example.cinema.domain.entities;

import java.util.Set;
import java.util.HashSet;

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

    public User() {
    }

    public User(String id, String username, String password, String email, Set<Role> roles, Set<Permission> permissions, String activeToken, boolean isBlocked, Long tokenVersion) {
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
    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static class UserBuilder {
        private String id;
        private String username;
        private String password;
        private String email;
        private Set<Role> roles = new HashSet<>();
        private Set<Permission> permissions = new HashSet<>();
        private String activeToken;
        private boolean isBlocked;
        private Long tokenVersion = 1L;

        public UserBuilder id(String id) {
            this.id = id;
            return this;
        }

        public UserBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UserBuilder password(String password) {
            this.password = password;
            return this;
        }

        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder roles(Set<Role> roles) {
            this.roles = roles;
            return this;
        }

        public UserBuilder permissions(Set<Permission> permissions) {
            this.permissions = permissions;
            return this;
        }

        public UserBuilder activeToken(String activeToken) {
            this.activeToken = activeToken;
            return this;
        }

        public UserBuilder isBlocked(boolean isBlocked) {
            this.isBlocked = isBlocked;
            return this;
        }

        public UserBuilder tokenVersion(Long tokenVersion) {
            this.tokenVersion = tokenVersion;
            return this;
        }

        public User build() {
            return new User(id, username, password, email, roles, permissions, activeToken, isBlocked, tokenVersion);
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

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
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
