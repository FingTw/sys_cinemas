package com.example.cinema.iam.application.dto;

/**
 * DTO cho request cập nhật thông tin cá nhân.
 * User chỉ được phép đổi email (username và role không được tự đổi).
 */
public class UpdateProfileRequest {
    private String email;

    public UpdateProfileRequest() {
    }

    public UpdateProfileRequest(String email) {
        this.email = email;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
