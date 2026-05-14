package com.example.cinema.application.ports.in;

public interface RevokeTokenUseCase {
    void revokeAllTokensForUser(String userId);
    void revokeSingleToken(String jti);
}
