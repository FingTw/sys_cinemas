package com.example.cinema.iam.application.ports.in;

import com.example.cinema.iam.application.dto.AuthResponse;
import com.example.cinema.iam.application.dto.RegisterRequest;

public interface AuthServicePort {
    AuthResponse login(String username, String password, String ipAddress, String userAgent);
    void register(RegisterRequest request);
    AuthResponse refreshToken(String refreshTokenJti, String ipAddress, String userAgent);
    void logout(String token);
    boolean checkUsername(String username);
    boolean checkEmail(String email);
}
