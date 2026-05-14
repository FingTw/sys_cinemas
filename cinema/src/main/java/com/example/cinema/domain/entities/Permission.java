package com.example.cinema.domain.entities;

import java.util.UUID;

public class Permission {
    private UUID id;
    private String name;
    private String description;

    public Permission() {
    }

    public Permission(UUID id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public static PermissionBuilder builder() {
        return new PermissionBuilder();
    }

    public static class PermissionBuilder {
        private UUID id;
        private String name;
        private String description;

        public PermissionBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public PermissionBuilder name(String name) {
            this.name = name;
            return this;
        }

        public PermissionBuilder description(String description) {
            this.description = description;
            return this;
        }

        public Permission build() {
            return new Permission(id, name, description);
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
