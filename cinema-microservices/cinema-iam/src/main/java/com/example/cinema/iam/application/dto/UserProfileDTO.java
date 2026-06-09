package com.example.cinema.iam.application.dto;

import java.util.Set;
import lombok.*;

/**
 * DTO cho User Profile — dùng để xem và cập nhật thông tin cá nhân.
 * Không bao giờ trả về password.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDTO {
    private String id;
    private String username;
    private String email;
    private Set<String> roles;
}
