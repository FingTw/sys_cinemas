package com.example.cinema.iam.infrastructure.database.entities;

import jakarta.persistence.*;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "permissions", schema = "auth")
@Getter
@Setter
public class PermissionJpaEntity {
    @Id
    private UUID id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    public PermissionJpaEntity() {
    }

    public PermissionJpaEntity(UUID id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

}
