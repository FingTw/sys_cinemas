package com.example.cinema.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình Fallback cho Redis Cache.
 * Khi Redis bị sập (Crash), Spring Boot mặc định sẽ ném ra Exception làm gián đoạn API (500 Error).
 * Cấu hình này sẽ "bắt" các lỗi đó, chỉ ghi Log cảnh báo (Warning),
 * và cho phép hệ thống tự động BỎ QUA cache để GỌI THẲNG XUỐNG DATABASE.
 * Giúp hệ thống đạt tính năng "Partial Failure" thay vì "Total Failure".
 */
@Configuration
public class RedisResilienceConfig implements CachingConfigurer {

    private static final Logger log = LoggerFactory.getLogger(RedisResilienceConfig.class);

    @Override
    public CacheErrorHandler errorHandler() {
        return new CustomCacheErrorHandler();
    }

    public static class CustomCacheErrorHandler implements CacheErrorHandler {

        @Override
        public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
            log.warn("[REDIS-FALLBACK] Khong the GET tu Redis (Cache: {}). Fallback query DB. Chi tiet: {}", cache.getName(), exception.getMessage());
        }

        @Override
        public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
            log.warn("[REDIS-FALLBACK] Khong the PUT vao Redis (Cache: {}). Chi tiet: {}", cache.getName(), exception.getMessage());
        }

        @Override
        public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
            log.warn("[REDIS-FALLBACK] Khong the EVICT tu Redis (Cache: {}). Chi tiet: {}", cache.getName(), exception.getMessage());
        }

        @Override
        public void handleCacheClearError(RuntimeException exception, Cache cache) {
            log.warn("[REDIS-FALLBACK] Khong the CLEAR Redis (Cache: {}). Chi tiet: {}", cache.getName(), exception.getMessage());
        }
    }
}
