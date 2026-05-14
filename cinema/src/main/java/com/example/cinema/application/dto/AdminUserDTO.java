package com.example.cinema.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AdminUserDTO {
    private String id;
    private String username;
    private String email;
    private String roles;
    private String permissions;

    @JsonProperty("isBlocked")
    private boolean isBlocked;

    @JsonProperty("isOnline")
    private boolean isOnline;

    public AdminUserDTO() {
    }

    public AdminUserDTO(String id, String username, String email, String roles, String permissions, boolean isBlocked, boolean isOnline) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.permissions = permissions;
        this.isBlocked = isBlocked;
        this.isOnline = isOnline;
    }

    // Builder manual
    public static AdminUserDTOBuilder builder() {
        return new AdminUserDTOBuilder();
    }

    public static class AdminUserDTOBuilder {
        private String id;
        private String username;
        private String email;
        private String roles;
        private String permissions;
        private boolean isBlocked;
        private boolean isOnline;

        public AdminUserDTOBuilder id(String id) {
            this.id = id;
            return this;
        }

        public AdminUserDTOBuilder username(String username) {
            this.username = username;
            return this;
        }

        public AdminUserDTOBuilder email(String email) {
            this.email = email;
            return this;
        }

        public AdminUserDTOBuilder roles(String roles) {
            this.roles = roles;
            return this;
        }

        public AdminUserDTOBuilder permissions(String permissions) {
            this.permissions = permissions;
            return this;
        }

        public AdminUserDTOBuilder isBlocked(boolean isBlocked) {
            this.isBlocked = isBlocked;
            return this;
        }

        public AdminUserDTOBuilder isOnline(boolean isOnline) {
            this.isOnline = isOnline;
            return this;
        }

        public AdminUserDTO build() {
            return new AdminUserDTO(id, username, email, roles, permissions, isBlocked, isOnline);
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    @JsonProperty("isBlocked")
    public boolean isBlocked() {
        return isBlocked;
    }

    @JsonProperty("isBlocked")
    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    @JsonProperty("isOnline")
    public boolean isOnline() {
        return isOnline;
    }

    @JsonProperty("isOnline")
    public void setOnline(boolean online) {
        isOnline = online;
    }
}
