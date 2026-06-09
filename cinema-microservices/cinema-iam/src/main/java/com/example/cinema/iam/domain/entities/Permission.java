package com.example.cinema.iam.domain.entities;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
public class Permission {
    private UUID id;
    private String name;
    private String description;

    @Builder
    public Permission(UUID id, String name, String description) {
        this.id = id != null ? id : java.util.UUID.randomUUID();
        this.name = name;
        this.description = description;
    }
}
