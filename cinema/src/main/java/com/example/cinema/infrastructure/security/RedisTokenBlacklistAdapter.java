package com.example.cinema.infrastructure.security;

import com.example.cinema.application.ports.out.TokenBlacklistPort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Primary;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Primary
public class RedisTokenBlacklistAdapter implements TokenBlacklistPort {

    private static final Logger log = LoggerFactory.getLogger(RedisTokenBlacklistAdapter.class);
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String BLACKLIST_PREFIX = "token:blacklist:";

    public RedisTokenBlacklistAdapter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void blacklistToken(String token, Date expiresAt) {
        long duration = expiresAt.getTime() - System.currentTimeMillis();
        if (duration > 0) {
            String key = BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(key, "revoked", duration, TimeUnit.MILLISECONDS);
            log.info("Token blacklisted in Redis for {} ms", duration);
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
        } catch (Exception e) {
            log.error("Redis error while checking blacklist: {}", e.getMessage());
            return false; // Fallback to allow if Redis is down (or you could use DB fallback)
        }
    }
}
