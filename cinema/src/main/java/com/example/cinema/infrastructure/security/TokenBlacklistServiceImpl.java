package com.example.cinema.infrastructure.security;

import com.example.cinema.application.ports.out.TokenBlacklistPort;

import com.example.cinema.infrastructure.database.entities.TokenBlacklist;
import com.example.cinema.infrastructure.database.repositories.TokenBlacklistRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistPort {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistServiceImpl.class);

    private final TokenBlacklistRepository tokenBlacklistRepository;

    public TokenBlacklistServiceImpl(TokenBlacklistRepository tokenBlacklistRepository) {
        this.tokenBlacklistRepository = tokenBlacklistRepository;
    }

    public void blacklistToken(String token, Date expiresAt) {
        if (token != null && !token.isEmpty()) {
            TokenBlacklist blacklisted = TokenBlacklist.builder()
                    .token(token)
                    .expiresAt(expiresAt)
                    .build();
            tokenBlacklistRepository.save(blacklisted);
        }
    }

    public boolean isBlacklisted(String token) {
        return tokenBlacklistRepository.existsById(token);
    }

    // Chay ngam moi dem (hoac moi gio) de don dep cac token da het han tu nhien
    @Scheduled(fixedRate = 3600000) // Chay moi gio
    @Transactional
    public void cleanupExpiredTokens() {
        tokenBlacklistRepository.deleteExpiredTokens(new Date());
    }
}
