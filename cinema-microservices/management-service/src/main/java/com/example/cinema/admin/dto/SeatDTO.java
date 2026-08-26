package com.example.cinema.admin.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatDTO {
    private String id;
    private String roomId;
    private String rowLabel;
    private Integer colNumber;
    private String type;
    private String status;
}
