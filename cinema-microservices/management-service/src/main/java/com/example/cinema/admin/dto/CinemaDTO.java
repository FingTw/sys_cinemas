package com.example.cinema.admin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CinemaDTO {
    private String id;
    private String name;
    private String address;
    private String complexId;
    private String complexName;
}
