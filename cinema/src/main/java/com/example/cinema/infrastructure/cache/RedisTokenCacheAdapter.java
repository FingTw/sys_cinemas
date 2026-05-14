package com.example.cinema.infrastructure.cache;

import com.example.cinema.application.ports.out.TokenCachePort;
import com.example.cinema.application.ports.out.UserRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisTokenCacheAdapter implements TokenCachePort {

    private static final Logger log = LoggerFactory.getLogger(RedisTokenCacheAdapter.class);
    private static final String VERSION_KEY_PREFIX = "user:token_version:";

    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepositoryPort userRepositoryPort;

    public RedisTokenCacheAdapter(RedisTemplate<String, String> redisTemplate, UserRepositoryPort userRepositoryPort) {
        this.redisTemplate = redisTemplate;
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public Long getUserTokenVersion(String userId) {
        String key = VERSION_KEY_PREFIX + userId;
        try {
            String cachedVersion = redisTemplate.opsForValue().get(key);
            if (cachedVersion != null) {
                return Long.parseLong(cachedVersion);
            }
        } catch (Exception e) {
            log.error("Redis is down or timeout while getting token version for user {}. Fallback to DB.", userId, e);
        }

        // Fallback: Đọc thẳng từ DB
        Long dbVersion = userRepositoryPort.findTokenVersionById(userId);
        
        // Cố gắng cache lại (Cache-Aside)
        try {
            if (dbVersion != null) {
                redisTemplate.opsForValue().set(key, String.valueOf(dbVersion), 24, TimeUnit.HOURS);
            }
        } catch (Exception e) {
            log.warn("Could not set cache for user {}. Redis might be down.", userId);
        }
        
        return dbVersion != null ? dbVersion : 1L;
    }

    @Override
    public void incrementTokenVersion(String userId) {
        String key = VERSION_KEY_PREFIX + userId;
        // Bắt buộc update DB trước tiên để đảm bảo tính nhất quán
        Long newVersion = userRepositoryPort.incrementTokenVersion(userId);
        
        // Sau đó update Redis
        try {
            if (newVersion != null) {
                redisTemplate.opsForValue().set(key, String.valueOf(newVersion), 24, TimeUnit.HOURS);
            }
        } catch (Exception e) {
            log.error("Failed to update token version in Redis for user {}. DB is already updated to version {}.", userId, newVersion, e);
        }
    }

    private static final String VALID_TOKEN_PREFIX = "token:valid:";

    @Override
    public void cacheToken(String token, String userId, long expirationMs) {
        String key = VALID_TOKEN_PREFIX + token;
        try {
            redisTemplate.opsForValue().set(key, userId, expirationMs, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("Failed to cache token in Redis: {}", e.getMessage());
        }
    }

    @Override
    public boolean isValidToken(String token) {
        String key = VALID_TOKEN_PREFIX + token;
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Redis error while checking token validity: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void removeToken(String token) {
        String key = VALID_TOKEN_PREFIX + token;
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Failed to remove token from Redis: {}", e.getMessage());
        }
    }

    private static final String ROLES_KEY_PREFIX = "user:roles:";
    private static final String PERMS_KEY_PREFIX = "user:perms:";

    @Override
    public void cacheUserRoles(String userId, String roles) {
        String key = ROLES_KEY_PREFIX + userId;
        try {
            redisTemplate.opsForValue().set(key, roles, 24, TimeUnit.HOURS);
            log.info("Cached roles for user {} in Redis", userId);
        } catch (Exception e) {
            log.error("Failed to cache roles in Redis for user {}: {}", userId, e.getMessage());
        }
    }

    @Override
    public String getUserRoles(String userId) {
        String key = ROLES_KEY_PREFIX + userId;
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Failed to get roles from Redis for user {}: {}", userId, e.getMessage());
            return null;
        }
    }

    @Override
    public void cacheUserPermissions(String userId, String permissions) {
        String key = PERMS_KEY_PREFIX + userId;
        try {
            redisTemplate.opsForValue().set(key, permissions, 24, TimeUnit.HOURS);
            log.info("Cached permissions for user {} in Redis", userId);
        } catch (Exception e) {
            log.error("Failed to cache permissions in Redis for user {}: {}", userId, e.getMessage());
        }
    }

    @Override
    public String getUserPermissions(String userId) {
        String key = PERMS_KEY_PREFIX + userId;
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Failed to get permissions from Redis for user {}: {}", userId, e.getMessage());
            return null;
        }
    }
}
