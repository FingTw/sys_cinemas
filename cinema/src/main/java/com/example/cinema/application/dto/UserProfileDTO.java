package com.example.cinema.application.dto;

import java.util.Set;

/**
 * DTO cho User Profile — dùng để xem và cập nhật thông tin cá nhân.
 * Không bao giờ trả về password.
 */
public class UserProfileDTO {
    private String id;
    private String username;
    private String email;
    private Set<String> roles;

    public UserProfileDTO() {
    }

    public UserProfileDTO(String id, String username, String email, Set<String> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }

    // Builder
    public static UserProfileDTOBuilder builder() {
        return new UserProfileDTOBuilder();
    }

    public static class UserProfileDTOBuilder {
        private String id;
        private String username;
        private String email;
        private Set<String> roles;

        public UserProfileDTOBuilder id(String id) { this.id = id; return this; }
        public UserProfileDTOBuilder username(String username) { this.username = username; return this; }
        public UserProfileDTOBuilder email(String email) { this.email = email; return this; }
        public UserProfileDTOBuilder roles(Set<String> roles) { this.roles = roles; return this; }

        public UserProfileDTO build() {
            return new UserProfileDTO(id, username, email, roles);
        }
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
}
