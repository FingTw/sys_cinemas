package com.example.cinema.infrastructure.database.adapters;

import com.example.cinema.application.ports.out.TokenRepositoryPort;
import com.example.cinema.domain.entities.AuthToken;
import com.example.cinema.infrastructure.database.entities.AuthTokenJpaEntity;
import com.example.cinema.infrastructure.database.repositories.SpringDataTokenRepository;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.Optional;

@Component
public class PostgresTokenAdapter implements TokenRepositoryPort {

    private final SpringDataTokenRepository springDataTokenRepository;

    public PostgresTokenAdapter(SpringDataTokenRepository springDataTokenRepository) {
        this.springDataTokenRepository = springDataTokenRepository;
    }

    @Override
    public Optional<AuthToken> findByJti(String jti) {
        return springDataTokenRepository.findByTokenJti(jti).map(entity -> 
            AuthToken.builder()
                .id(entity.getId() != null ? entity.getId().toString() : null)
                .userId(entity.getUserId())
                .tokenJti(entity.getTokenJti())
                .tokenHash(entity.getTokenHash())
                .ipAddress(entity.getIpAddress())
                .userAgent(entity.getUserAgent())
                .isActive(entity.isActive())
                .issuedAt(entity.getIssuedAt())
                .expiresAt(entity.getExpiresAt())
                .revokedAt(entity.getRevokedAt())
                .build()
        );
    }

    @Override
    public void save(AuthToken token) {
        AuthTokenJpaEntity entity = new AuthTokenJpaEntity(
            token.getId() != null ? UUID.fromString(token.getId()) : null,
            token.getUserId(),
            token.getTokenJti(),
            token.getTokenHash(),
            token.getIpAddress(),
            token.getUserAgent(),
            token.isActive(),
            token.getIssuedAt(),
            token.getExpiresAt(),
            token.getRevokedAt()
        );
        springDataTokenRepository.save(entity);
    }

    @Override
    public void revokeAllByUserId(String userId) {
        springDataTokenRepository.revokeAllByUserId(userId, ZonedDateTime.now());
    }

    @Override
    public void revokeTokenByJti(String jti) {
        springDataTokenRepository.revokeByJti(jti, ZonedDateTime.now());
    }
}
