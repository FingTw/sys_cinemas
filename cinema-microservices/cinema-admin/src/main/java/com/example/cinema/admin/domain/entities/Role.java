package com.example.cinema.admin.domain.entities;

import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
public class Role {
    private UUID id;
    private String name;
    private Set<Permission> permissions = new HashSet<>();

    @Builder
    public Role(UUID id, String name, Set<Permission> permissions) {
        this.id = id != null ? id : java.util.UUID.randomUUID();
        this.name = name;
        this.permissions = permissions != null ? permissions : new HashSet<>();
    }

    public void updatePermissions(Set<Permission> permissions) {
        this.permissions = permissions != null ? permissions : new HashSet<>();
    }
}
