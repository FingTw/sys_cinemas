package com.example.cinema.admin.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CinemaComplexDTO {
    private String id;
    private String name;
    private String description;
}
