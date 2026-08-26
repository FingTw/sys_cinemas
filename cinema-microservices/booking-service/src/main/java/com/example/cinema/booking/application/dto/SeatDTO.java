package com.example.cinema.booking.application.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatDTO {
    private String id;
    private String roomId;
    private String rowLabel;
    private int colNumber;
    private String type; // VIP, STANDARD, COUPLE
    private String status;
}
