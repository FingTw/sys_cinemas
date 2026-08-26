package com.example.cinema.admin.dto;

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
    private Integer totalSeats;
    private String cinemaId;
    private String cinemaName;
    private String cinemaComplexName;
}
