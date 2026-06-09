package com.example.cinema.scheduling.application.dto.feign;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDTO {
    private String id;
    private String name;
    private Integer totalSeats;
    private String type;
    private boolean isActive;
}
