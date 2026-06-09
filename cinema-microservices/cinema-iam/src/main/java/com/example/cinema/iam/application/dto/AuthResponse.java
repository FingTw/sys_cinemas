package com.example.cinema.iam.application.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token; // Access Token
    private String refreshToken;
}
