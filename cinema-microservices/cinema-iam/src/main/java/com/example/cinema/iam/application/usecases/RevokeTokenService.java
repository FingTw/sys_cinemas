package com.example.cinema.iam.application.usecases;

import com.example.cinema.iam.application.ports.in.RevokeTokenUseCase;
import com.example.cinema.iam.domain.repositories.TokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RevokeTokenService implements RevokeTokenUseCase {

    private final TokenRepository tokenRepositoryPort;

    public RevokeTokenService(TokenRepository tokenRepositoryPort) {
        this.tokenRepositoryPort = tokenRepositoryPort;
    }

    @Override
    @Transactional
    public void revokeAllTokensForUser(String userId) {
        tokenRepositoryPort.revokeAllByUserId(userId);
    }

    @Override
    @Transactional
    public void revokeSingleToken(String jti) {
        tokenRepositoryPort.revokeTokenByJti(jti);
    }
}