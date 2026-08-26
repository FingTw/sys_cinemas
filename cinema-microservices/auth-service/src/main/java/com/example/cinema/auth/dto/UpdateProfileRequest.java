package com.example.cinema.auth.dto;

import lombok.*;

/**
 * DTO cho request cập nhật thông tin cá nhân.
 * User chỉ được phép đổi email (username và role không được tự đổi).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {
    private String email;
}
