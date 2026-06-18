package com.example.cinema.admin.domain.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
public class CinemaComplex {
    private String id;
    private String name;
    private String description;

    @Builder
    public CinemaComplex(String id, String name, String description) {
        this.id = (id != null && !id.trim().isEmpty()) ? id : java.util.UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
    }

    public void updateDetails(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
