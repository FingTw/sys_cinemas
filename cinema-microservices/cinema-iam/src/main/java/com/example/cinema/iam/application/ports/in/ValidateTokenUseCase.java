package com.example.cinema.iam.application.ports.in;

public interface ValidateTokenUseCase {
    boolean isValidVersion(String userId, Long tokenVersionInJwt);
}
