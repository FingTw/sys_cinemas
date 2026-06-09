package com.example.cinema.admin.domain.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
public class Room {
    private String id;
    private String name;
    private String status; // ACTIVE, MAINTENANCE
    private Integer gridRows;
    private Integer gridCols;

    @Builder
    public Room(String id, String name, String status, Integer gridRows, Integer gridCols) {
        this.id = (id != null && !id.trim().isEmpty()) ? id : java.util.UUID.randomUUID().toString();
        this.name = name;
        this.status = status;
        this.gridRows = gridRows;
        this.gridCols = gridCols;
    }

    public void updateDetails(String name, String status, Integer gridRows, Integer gridCols) {
        this.name = name;
        this.status = status;
        this.gridRows = gridRows;
        this.gridCols = gridCols;
    }
}
