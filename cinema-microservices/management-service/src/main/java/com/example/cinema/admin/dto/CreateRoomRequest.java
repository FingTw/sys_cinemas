package com.example.cinema.admin.dto;

import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRoomRequest {
    private String name;
    private Integer gridRows;
    private Integer gridCols;
    private List<SeatDTO> seats;
    private String cinemaId;
}
