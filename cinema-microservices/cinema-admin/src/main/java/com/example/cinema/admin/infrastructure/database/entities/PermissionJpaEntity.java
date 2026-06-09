package com.example.cinema.admin.infrastructure.database.entities;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "permissions", schema = "auth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PermissionJpaEntity {
    @Id
    private UUID id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;
}
