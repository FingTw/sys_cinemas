package com.example.cinema.facility.domain.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
public class Cinema {
    private String id;
    private String name;
    private String address;
    private String complexId;

    @Builder
    public Cinema(String id, String name, String address, String complexId) {
        this.id = (id != null && !id.trim().isEmpty()) ? id : java.util.UUID.randomUUID().toString();
        this.name = name;
        this.address = address;
        this.complexId = complexId;
    }
}
