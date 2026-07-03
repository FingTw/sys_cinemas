package com.example.cinema.booking.application.dto;

import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBookingRequest {
    private String showtimeId;
    private List<String> seatIds;
    private List<BookingItemRequest> items; // F&B items
    private String paymentMethod; // ONLINE or COUNTER
}
