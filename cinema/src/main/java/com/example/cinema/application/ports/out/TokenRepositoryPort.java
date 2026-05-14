package com.example.cinema.application.ports.out;

import com.example.cinema.domain.entities.AuthToken;
import java.util.Optional;

public interface TokenRepositoryPort {
    void save(AuthToken token);
    Optional<AuthToken> findByJti(String jti);
    void revokeAllByUserId(String userId);
    void revokeTokenByJti(String jti);
}
