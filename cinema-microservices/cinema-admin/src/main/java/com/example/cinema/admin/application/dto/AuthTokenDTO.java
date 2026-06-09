package com.example.cinema.admin.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthTokenDTO {
    private UUID id;
    private String userId;
    private String username;
    private String email;
    private String ipAddress;
    private String userAgent;
    private boolean isActive;
    private ZonedDateTime issuedAt;
    private ZonedDateTime expiresAt;
    private ZonedDateTime revokedAt;
}
