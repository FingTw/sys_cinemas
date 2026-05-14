package com.example.cinema.application.ports.in;

import com.example.cinema.application.dto.UpdateProfileRequest;
import com.example.cinema.application.dto.UserProfileDTO;

/**
 * Port cho chức năng xem/cập nhật thông tin cá nhân của User.
 */
public interface UserProfileUseCase {
    UserProfileDTO getProfile(String userId);
    UserProfileDTO updateProfile(String userId, UpdateProfileRequest request);
}
