package com.example.cinema.facility.domain.entities;

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
    private String cinemaId;

    @Builder
    public Room(String id, String name, String status, Integer gridRows, Integer gridCols, String cinemaId) {
        this.id = (id != null && !id.trim().isEmpty()) ? id : java.util.UUID.randomUUID().toString();
        this.name = name;
        this.status = status;
        this.gridRows = gridRows;
        this.gridCols = gridCols;
        this.cinemaId = cinemaId;
    }

    public void updateStatus(String newStatus) {
        if (!newStatus.equals("ACTIVE") && !newStatus.equals("MAINTENANCE")) {
            throw new IllegalArgumentException("Invalid room status: " + newStatus);
        }
        this.status = newStatus;
    }
}
