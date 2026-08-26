package com.example.cinema.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogTraceDto {
    private String timestamp;
    private String level;
    private String service;
    private String message;
    private String requestId;
}
