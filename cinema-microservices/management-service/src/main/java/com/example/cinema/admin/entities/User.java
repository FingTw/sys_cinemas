package com.example.cinema.admin.entities;

import jakarta.persistence.*;
import java.util.Set;
import java.util.HashSet;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "users", schema = "auth")
@SQLDelete(sql = "UPDATE auth.users SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Builder.Default
    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String username;



    @Column(unique = true, nullable = false)
    private String email;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles", schema = "auth", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_permissions",
        schema = "auth",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();



    @Builder.Default
    @Column(nullable = false)
    private boolean isBlocked = false;

    @Column(name = "cinema_id")
    private String cinemaId;


    public void updateRoleAndPermissions(java.util.Set<Role> roles, java.util.Set<Permission> permissions) { this.roles = roles; this.permissions = permissions; }

    public void updateStatus(boolean isBlocked) { this.isBlocked = isBlocked; }

}
