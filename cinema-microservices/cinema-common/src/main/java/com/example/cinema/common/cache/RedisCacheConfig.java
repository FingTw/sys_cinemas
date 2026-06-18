package com.example.cinema.common.cache;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Cau hinh Redis Cache dung chung cho tat ca microservices.
 * Tu dong kich hoat khi co spring-data-redis tren classpath.
 *
 * Cache Names & TTL:
 * - movies        : 10 phut  (danh sach phim it thay doi)
 * - movie         : 15 phut  (chi tiet 1 phim)
 * - showtimes     : 5 phut   (lich chieu thay doi thuong xuyen hon)
 * - showtime      : 5 phut
 * - rooms         : 30 phut  (phong chieu rat it thay doi)
 * - room          : 30 phut
 * - seats         : 30 phut  (so do ghe cung it thay doi)
 * - default       : 10 phut
 */
@Configuration
@EnableCaching
@ConditionalOnClass(RedisConnectionFactory.class)
public class RedisCacheConfig {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheConfig.class);

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        log.info(">>> Khoi tao Redis CacheManager voi cau hinh TTL tuy chinh <<<");

        // ObjectMapper ho tro Java 8 Date/Time (LocalDate, LocalDateTime, BigDecimal...)
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        // Cau hinh mac dinh: TTL 10 phut
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues();

        // Cau hinh rieng cho tung cache name
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

        // --- Catalog Service ---
        cacheConfigs.put("movies", defaultConfig.entryTtl(Duration.ofSeconds(5)));
        cacheConfigs.put("movie", defaultConfig.entryTtl(Duration.ofSeconds(5)));
        cacheConfigs.put("movieCount", defaultConfig.entryTtl(Duration.ofSeconds(5)));

        // --- Scheduling Service ---
        cacheConfigs.put("showtimes", defaultConfig.entryTtl(Duration.ofSeconds(5)));
        cacheConfigs.put("showtime", defaultConfig.entryTtl(Duration.ofSeconds(5)));
        cacheConfigs.put("showtimesByMovie", defaultConfig.entryTtl(Duration.ofSeconds(5)));

        // --- Facility Service ---
        cacheConfigs.put("rooms", defaultConfig.entryTtl(Duration.ofSeconds(5)));
        cacheConfigs.put("room", defaultConfig.entryTtl(Duration.ofSeconds(5)));
        cacheConfigs.put("seats", defaultConfig.entryTtl(Duration.ofSeconds(5)));

        log.info(">>> Cache TTL: movies=5s, movie=5s, showtimes=5s, rooms=5s <<<");

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .transactionAware()
                .build();
    }
}
