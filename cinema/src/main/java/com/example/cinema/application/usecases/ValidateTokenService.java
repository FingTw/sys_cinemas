package com.example.cinema.application.usecases;

import com.example.cinema.application.ports.in.ValidateTokenUseCase;
import com.example.cinema.application.ports.out.TokenCachePort;
import org.springframework.stereotype.Service;

@Service
public class ValidateTokenService implements ValidateTokenUseCase {

    private final TokenCachePort tokenCachePort;

    public ValidateTokenService(TokenCachePort tokenCachePort) {
        this.tokenCachePort = tokenCachePort;
    }

    @Override
    public boolean isValidVersion(String userId, Long tokenVersionInJwt) {
        // Lấy version từ Redis (với fallback DB)
        Long currentVersion = tokenCachePort.getUserTokenVersion(userId);
        
        // Nếu version trong Token không khớp version trong hệ thống -> Token cũ, đã bị thu hồi
        return currentVersion != null && currentVersion.equals(tokenVersionInJwt);
    }
}
