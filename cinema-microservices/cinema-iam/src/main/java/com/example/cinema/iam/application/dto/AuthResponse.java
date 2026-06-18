package com.example.cinema.iam.application.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token; // Access Token (sẽ bị Gateway bóc ra khỏi body, gắn vào Cookie)
    private String refreshToken;
    private String username;  // Thông tin user để Frontend hiển thị (không cần decode JWT)
    private String roles;     // Roles dạng chuỗi (VD: "ADMIN,STAFF")
    private String permissions; // Permissions dạng chuỗi (VD: "USER_MANAGE,MOVIE_READ")
    private String userId;    // User ID
}
