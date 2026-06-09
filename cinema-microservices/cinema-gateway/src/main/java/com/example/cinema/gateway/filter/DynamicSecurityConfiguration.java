package com.example.cinema.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Configuration
public class DynamicSecurityConfiguration {

    private static final Logger log = LoggerFactory.getLogger(DynamicSecurityConfiguration.class);

    private final AtomicReference<String> clientKeyRef = new AtomicReference<>("cinema-secret-fe-key-2026");
    private final AtomicReference<List<String>> protectedPathsRef = new AtomicReference<>(List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/public-key",
            "/api/v1/auth/refresh-token",
            "/api/v1/auth/password-policy",
            "/api/v1/public"
    ));

    @Value("${ADMIN_SERVICE_URL}")
    private String adminServiceUrl;

    @Value("${APP_SECURITY_INTERNAL_API_KEY}")
    private String internalApiKey;

    @Value("${cinema.security.client-key:${API_KEY}}")
    private String defaultClientKey;

    @PostConstruct
    public void init() {
        // Apply default configs from env/properties first
        clientKeyRef.set(defaultClientKey);
        
        // Fetch from IAM asynchronously at startup
        refreshSecurityConfig();
    }

    public void refreshSecurityConfig() {
        WebClient.builder().baseUrl(adminServiceUrl).build()
            .get()
            .uri("/api/v1/internal/security-config")
            .header("X-Internal-Api-Key", internalApiKey)
            .retrieve()
            .bodyToMono(SecurityConfigDto.class)
            .subscribe(dto -> {
                if (dto != null) {
                    if (dto.getClientKey() != null && !dto.getClientKey().trim().isEmpty()) {
                        clientKeyRef.set(dto.getClientKey());
                    }
                    if (dto.getGatewayProtectedPaths() != null && !dto.getGatewayProtectedPaths().trim().isEmpty()) {
                        String[] paths = dto.getGatewayProtectedPaths().split(",");
                        List<String> pathList = new ArrayList<>();
                        for (String p : paths) {
                            if (!p.trim().isEmpty()) {
                                pathList.add(p.trim());
                            }
                        }
                        protectedPathsRef.set(pathList);
                    }
                    log.info("Dynamic Security Configuration updated: clientKey has been refreshed, protectedPaths count={}", 
                            protectedPathsRef.get().size());
                }
            }, err -> log.warn("Admin Service is currently unavailable for Security Config sync. Using fallback/default security configuration. (Reason: {})", err.getMessage()));
    }

    public String getExpectedClientKey() {
        return clientKeyRef.get();
    }

    public List<String> getProtectedPaths() {
        return protectedPathsRef.get();
    }

    static class SecurityConfigDto {
        private String clientKey;
        private String gatewayProtectedPaths;
        private String serviceBypassPaths;

        public String getClientKey() { return clientKey; }
        public void setClientKey(String clientKey) { this.clientKey = clientKey; }
        public String getGatewayProtectedPaths() { return gatewayProtectedPaths; }
        public void setGatewayProtectedPaths(String gatewayProtectedPaths) { this.gatewayProtectedPaths = gatewayProtectedPaths; }
        public String getServiceBypassPaths() { return serviceBypassPaths; }
        public void setServiceBypassPaths(String serviceBypassPaths) { this.serviceBypassPaths = serviceBypassPaths; }
    }
}
