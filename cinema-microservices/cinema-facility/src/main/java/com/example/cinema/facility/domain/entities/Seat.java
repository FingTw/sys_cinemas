package com.example.cinema.facility.domain.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
public class Seat {
    private String id;
    private String roomId;
    private String rowLabel; // A, B, C...
    private Integer colNumber; // 1, 2, 3...
    private String type; // STANDARD, VIP, COUPLE
    private String status; // ACTIVE, BROKEN

    @Builder
    public Seat(String id, String roomId, String rowLabel, Integer colNumber, String type, String status) {
        this.id = (id != null && !id.trim().isEmpty()) ? id : java.util.UUID.randomUUID().toString();
        this.roomId = roomId;
        this.rowLabel = rowLabel;
        this.colNumber = colNumber;
        this.type = type;
        this.status = status;
    }

    public void updateStatus(String newStatus) {
        if (!newStatus.equals("ACTIVE") && !newStatus.equals("BROKEN")) {
            throw new IllegalArgumentException("Invalid seat status: " + newStatus);
        }
        this.status = newStatus;
    }
}
