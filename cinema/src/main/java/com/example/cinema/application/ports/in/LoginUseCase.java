package com.example.cinema.application.ports.in;

import com.example.cinema.application.dto.AuthResponse;
import com.example.cinema.domain.entities.User;

public interface LoginUseCase {
    AuthResponse login(String username, String encryptedPassword);
    String issueTokens(User user, String ipAddress, String userAgent);
}
