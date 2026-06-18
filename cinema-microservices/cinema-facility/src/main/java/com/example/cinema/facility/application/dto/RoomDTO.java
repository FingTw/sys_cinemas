package com.example.cinema.facility.application.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDTO {
    private String id;
    private String name;
    private String status;
    private Integer gridRows;
    private Integer gridCols;
    private Integer totalSeats; // Computed value from seats
    private String cinemaId;
    private String cinemaName;
    private String cinemaComplexName;
}
