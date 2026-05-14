package com.example.cinema.iam.infrastructure.database.entities;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "roles", schema = "auth")
@SQLDelete(sql = "UPDATE auth.roles SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
public class RoleJpaEntity {
    @jakarta.persistence.Column(name = "is_deleted")
    private boolean isDeleted = false;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permissions",
        schema = "auth",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<PermissionJpaEntity> permissions = new HashSet<>();

    public RoleJpaEntity() {
    }

    public RoleJpaEntity(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<PermissionJpaEntity> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<PermissionJpaEntity> permissions) {
        this.permissions = permissions;
    }
}
