package com.example.cinema.admin.application.dto;

import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBookingRequest {
    private String showtimeId;
    private List<String> seatIds;
}
