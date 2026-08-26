package com.example.cinema.admin.adapters;

import com.example.cinema.admin.services.CachePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisCacheAdapter implements CachePort {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void set(String key, String value, Duration timeout) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout);
        } catch (Exception e) {
            log.error("RedisCacheAdapter: Failed to set key [{}]: {}", key, e.getMessage(), e);
        }
    }

    @Override
    public void set(String key, String value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("RedisCacheAdapter: Failed to set key [{}]: {}", key, e.getMessage(), e);
        }
    }

    @Override
    public Boolean setIfAbsent(String key, String value, Duration timeout) {
        try {
            return redisTemplate.opsForValue().setIfAbsent(key, value, timeout);
        } catch (Exception e) {
            log.error("RedisCacheAdapter: Failed to setIfAbsent key [{}]: {}", key, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("RedisCacheAdapter: Failed to delete key [{}]: {}", key, e.getMessage(), e);
        }
    }

    @Override
    public void delete(java.util.Collection<String> keys) {
        try {
            redisTemplate.delete(keys);
        } catch (Exception e) {
            log.error("RedisCacheAdapter: Failed to delete keys: {}", e.getMessage(), e);
        }
    }

    @Override
    public String get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("RedisCacheAdapter: Failed to get key [{}]: {}", key, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public java.util.Set<String> keys(String pattern) {
        try {
            return redisTemplate.keys(pattern);
        } catch (Exception e) {
            log.error("RedisCacheAdapter: Failed to get keys for pattern [{}]: {}", pattern, e.getMessage(), e);
            return java.util.Collections.emptySet();
        }
    }
}
