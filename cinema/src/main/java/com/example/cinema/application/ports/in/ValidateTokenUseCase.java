package com.example.cinema.application.ports.in;

public interface ValidateTokenUseCase {
    boolean isValidVersion(String userId, Long tokenVersionInJwt);
}
