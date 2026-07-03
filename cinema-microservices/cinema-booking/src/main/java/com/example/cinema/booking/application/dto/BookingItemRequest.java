package com.example.cinema.booking.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingItemRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String productId;
    private Integer quantity;
}
