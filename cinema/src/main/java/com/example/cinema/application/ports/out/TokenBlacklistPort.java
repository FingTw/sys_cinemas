package com.example.cinema.application.ports.out;

import java.util.Date;

public interface TokenBlacklistPort {
    void blacklistToken(String token, Date expiresAt);
    boolean isBlacklisted(String token);
}
