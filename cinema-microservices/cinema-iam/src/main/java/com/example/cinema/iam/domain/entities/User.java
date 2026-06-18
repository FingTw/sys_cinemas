package com.example.cinema.iam.domain.entities;

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
    private String authProvider = "local";  // "local" | "keycloak"
    private String ssoSubject;              // Keycloak sub UUID (bất biến)
    private String cinemaId;                // ID của rạp phim mà user (nhân viên) làm việc

    @Builder
    public User(String id, String username, String password, String email, Set<Role> roles, Set<Permission> permissions, boolean isBlocked, String authProvider, String ssoSubject, String cinemaId) {
        this.id = (id != null && !id.trim().isEmpty()) ? id : java.util.UUID.randomUUID().toString().replace("-", "");
        this.username = username;
        this.password = password;
        this.email = email;
        this.roles = roles != null ? roles : new HashSet<>();
        this.permissions = permissions != null ? permissions : new HashSet<>();
        this.isBlocked = isBlocked;
        this.authProvider = (authProvider != null && !authProvider.trim().isEmpty()) ? authProvider : "local";
        this.ssoSubject = ssoSubject;
        this.cinemaId = cinemaId;
    }

    public void updateEmail(String newEmail) {
        if (newEmail == null || !newEmail.contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        this.email = newEmail;
    }



    public void block() {
        this.isBlocked = true;
    }

    public void unblock() {
        this.isBlocked = false;
    }
}
