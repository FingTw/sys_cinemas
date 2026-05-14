package com.example.cinema.iam.domain.entities;

import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

public class Role {
    private UUID id;
    private String name;
    private Set<Permission> permissions = new HashSet<>();

    public Role() {
    }

    public Role(UUID id, String name, Set<Permission> permissions) {
        this.id = id;
        this.name = name;
        this.permissions = permissions != null ? permissions : new HashSet<>();
    }

    public static RoleBuilder builder() {
        return new RoleBuilder();
    }

    public static class RoleBuilder {
        private UUID id;
        private String name;
        private Set<Permission> permissions = new HashSet<>();

        public RoleBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public RoleBuilder name(String name) {
            this.name = name;
            return this;
        }

        public RoleBuilder permissions(Set<Permission> permissions) {
            this.permissions = permissions;
            return this;
        }

        public Role build() {
            return new Role(id, name, permissions);
        }
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

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }
}
