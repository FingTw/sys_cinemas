package com.example.cinema.iam.application.usecases;

import com.example.cinema.iam.application.ports.in.SystemSecurityConfigUseCase;
import com.example.cinema.iam.domain.entities.SystemSecurityConfig;
import com.example.cinema.iam.domain.repositories.SystemSecurityConfigRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SystemSecurityConfigServiceImpl implements SystemSecurityConfigUseCase, CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SystemSecurityConfigServiceImpl.class);

    private final SystemSecurityConfigRepository repository;
    private final StringRedisTemplate redisTemplate;

    @Value("${GATEWAY_URL}")
    private String gatewayUrl;

    @Value("${APP_SECURITY_INTERNAL_API_KEY}")
    private String internalApiKey;

    @Override
    @Transactional(readOnly = true)
    public SystemSecurityConfig getConfig() {
        return repository.findById("default-security")
                .orElseGet(() -> SystemSecurityConfig.builder()
                        .id("default-security")
                        .clientKey("my-secret-dev-api-key")
                        .gatewayProtectedPaths("/api/v1/auth/login,/api/v1/auth/register,/api/v1/auth/public-key,/api/v1/auth/refresh-token,/api/v1/public")
                        .serviceBypassPaths("/actuator,/v3/api-docs,/swagger-ui,/api/v1/auth/public-key,/api/v1/movies,/api/v1/showtimes,/api/v1/rooms,/api/v1/vnpay")
                        .updatedAt(LocalDateTime.now())
                        .build());
    }


    @Override
    public void run(String... args) {
        try {
            log.info(">> Start synchronizing System Security Configurations to Redis <<");
            SystemSecurityConfig config = getConfig();
            syncToRedis(config);
            log.info(">> Synchronized System Security Configurations to Redis successfully <<");
            
            // Notify Gateway on startup
            notifyGateway();
        } catch (Exception e) {
            log.error("Failed to sync security configuration to Redis on startup: {}", e.getMessage());
        }
    }

    private void syncToRedis(SystemSecurityConfig config) {
        try {
            redisTemplate.opsForValue().set("security:client-key", config.getClientKey());
            redisTemplate.opsForValue().set("security:bypass-paths", config.getServiceBypassPaths());
            log.info("Sync security configuration to Redis: keys updated.");
        } catch (Exception e) {
            log.warn("Could not sync security configuration to Redis: {}", e.getMessage());
        }
    }

    private void notifyGateway() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Internal-Api-Key", internalApiKey);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            restTemplate.exchange(gatewayUrl + "/internal/gateway/refresh-security", HttpMethod.POST, entity, Void.class);
            log.info("Notified API Gateway to refresh security configuration.");
        } catch (Exception e) {
            log.warn("Could not notify gateway to refresh security: {}", e.getMessage());
        }
    }
}
