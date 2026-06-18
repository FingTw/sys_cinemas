package com.example.cinema.catalog.domain.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
public class Genre {
    private String id;
    private String name;
    private String code;

    @Builder
    public Genre(String id, String name, String code) {
        this.id = id;
        this.name = name;
        this.code = code;
    }
}
