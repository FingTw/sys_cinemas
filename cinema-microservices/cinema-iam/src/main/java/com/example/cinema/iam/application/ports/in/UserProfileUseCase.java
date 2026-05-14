package com.example.cinema.iam.application.ports.in;

import com.example.cinema.iam.application.dto.UpdateProfileRequest;
import com.example.cinema.iam.application.dto.UserProfileDTO;

/**
 * Port cho chức năng xem/cập nhật thông tin cá nhân của User.
 */
public interface UserProfileUseCase {
    UserProfileDTO getProfile(String userId);
    UserProfileDTO updateProfile(String userId, UpdateProfileRequest request);
}
