package com.example.cinema.iam.application.ports.in;

import com.example.cinema.iam.application.dto.AuthResponse;
import com.example.cinema.iam.domain.entities.User;

public interface LoginUseCase {
    AuthResponse login(String username, String encryptedPassword);
    String issueTokens(User user, String ipAddress, String userAgent);
}
