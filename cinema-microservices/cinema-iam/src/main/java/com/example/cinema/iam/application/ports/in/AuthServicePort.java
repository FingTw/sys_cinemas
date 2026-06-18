package com.example.cinema.iam.application.ports.in;

import com.example.cinema.iam.application.dto.AuthResponse;
import com.example.cinema.iam.application.dto.RegisterRequest;

public interface AuthServicePort {
    AuthResponse login(String username, String password, String ipAddress, String userAgent);
    void register(RegisterRequest request);
    AuthResponse refreshToken(String refreshTokenJti, String ipAddress, String userAgent);
    void logout(String token);
    void logoutAll(String token);
    boolean checkUsername(String username);
    boolean checkEmail(String email);

    /**
     * Dang nhap bang SSO Keycloak Standalone.
     * Nhan truc tiep Keycloak JWT va Refresh Token tu Frontend, validate offline,
     * sau do dung JIT provisioning de lien ket/tao moi tai khoan trong DB
     * va phat Local JWT.
     *
     * @param keycloakJwt          JWT Token tu Keycloak.
     * @param keycloakRefreshToken Refresh Token tu Keycloak de dung cho backchannel logout.
     * @param ipAddress            IP cua nguoi dung (de ghi log va quan ly session).
     * @param userAgent            User-Agent cua trinh duyet.
     * @return AuthResponse chua Local JWT, Refresh Token va thong tin user.
     */
    AuthResponse loginWithSso(String keycloakJwt, String keycloakRefreshToken, String ipAddress, String userAgent);
}
