package com.example.cinema.iam.domain.repositories;

import com.example.cinema.iam.domain.entities.AuthToken;
import java.util.Optional;

public interface TokenRepository {
    Optional<AuthToken> findByJti(String jti);
    void save(AuthToken token);
    void revokeAllByUserId(String userId);
    void revokeTokenByJti(String jti);
    java.util.List<AuthToken> findAllByUserId(String userId);
    void deleteById(String id);
}
