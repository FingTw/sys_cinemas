package com.example.cinema.iam.application.usecases;

import com.example.cinema.iam.application.ports.in.ValidateTokenUseCase;
import com.example.cinema.iam.domain.repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class ValidateTokenService implements ValidateTokenUseCase {

    private final UserRepository userRepositoryPort;

    public ValidateTokenService(UserRepository userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public boolean isValidVersion(String userId, Long tokenVersionInJwt) {
        Long currentVersion = userRepositoryPort.findTokenVersionById(userId);
        return currentVersion != null && currentVersion.equals(tokenVersionInJwt);
    }
}