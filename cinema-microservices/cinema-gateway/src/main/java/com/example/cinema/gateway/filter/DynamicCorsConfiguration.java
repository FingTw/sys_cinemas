package com.example.cinema.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Configuration
public class DynamicCorsConfiguration {

    private static final Logger log = LoggerFactory.getLogger(DynamicCorsConfiguration.class);

    private final AtomicReference<CorsConfiguration> corsConfigRef = new AtomicReference<>(new CorsConfiguration());
    
    @Value("${ADMIN_SERVICE_URL}")
    private String adminServiceUrl;
    
    @Value("${APP_SECURITY_INTERNAL_API_KEY}")
    private String internalApiKey;

    @PostConstruct
    public void init() {
        // Default config fallback
        CorsConfiguration defaultCfg = new CorsConfiguration();
        defaultCfg.addAllowedOrigin("*");
        defaultCfg.addAllowedMethod("*");
        defaultCfg.addAllowedHeader("*");
        defaultCfg.setAllowCredentials(true);
        corsConfigRef.set(defaultCfg);
        
        // Fetch from IAM asynchronously at startup
        refreshCorsConfig();
    }

    public void refreshCorsConfig() {
        WebClient.builder().baseUrl(adminServiceUrl).build()
            .get()
            .uri("/api/v1/internal/cors-config")
            .header("X-Internal-Api-Key", internalApiKey)
            .retrieve()
            .bodyToMono(CorsConfigDto.class)
            .subscribe(dto -> {
                if (dto != null) {
                    CorsConfiguration cfg = new CorsConfiguration();
                    if (dto.getAllowedOrigins() != null && !dto.getAllowedOrigins().isEmpty()) {
                        cfg.setAllowedOrigins(Arrays.asList(dto.getAllowedOrigins().split(",")));
                    } else {
                        cfg.addAllowedOrigin("*");
                    }
                    
                    if (dto.getAllowedMethods() != null && !dto.getAllowedMethods().isEmpty()) {
                        cfg.setAllowedMethods(Arrays.asList(dto.getAllowedMethods().split(",")));
                    } else {
                        cfg.addAllowedMethod("*");
                    }
                    
                    if (dto.getAllowedHeaders() != null && !dto.getAllowedHeaders().isEmpty()) {
                        cfg.setAllowedHeaders(Arrays.asList(dto.getAllowedHeaders().split(",")));
                    } else {
                        cfg.addAllowedHeader("*");
                    }
                    cfg.setAllowCredentials(true);
                    corsConfigRef.set(cfg);
                    log.info("Dynamic CORS Configuration updated: Origins={}, Methods={}", dto.getAllowedOrigins(), dto.getAllowedMethods());
                }
            }, err -> log.warn("Admin Service is currently unavailable for CORS Config sync. Using fallback/default CORS configuration. (Reason: {})", err.getMessage()));
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public WebFilter corsFilter() {
        return (ServerWebExchange exchange, WebFilterChain chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // Allow refresh endpoint to bypass CORS preflight
            if (request.getURI().getPath().equals("/internal/gateway/refresh-cors")) {
                return chain.filter(exchange);
            }

            if (CorsUtils.isCorsRequest(request)) {
                CorsConfiguration config = corsConfigRef.get();
                ServerHttpResponse response = exchange.getResponse();
                
                String origin = request.getHeaders().getOrigin();
                if (origin != null) {
                    boolean originAllowed = config.getAllowedOrigins().contains("*") || config.getAllowedOrigins().contains(origin);
                    if (originAllowed) {
                        response.getHeaders().add("Access-Control-Allow-Origin", origin);
                    }
                }
                
                if (config.getAllowCredentials() != null && config.getAllowCredentials()) {
                    response.getHeaders().add("Access-Control-Allow-Credentials", "true");
                }

                if (CorsUtils.isPreFlightRequest(request)) {
                    HttpMethod requestMethod = request.getHeaders().getAccessControlRequestMethod();
                    if (requestMethod != null) {
                        boolean methodAllowed = config.getAllowedMethods().contains("*") || config.getAllowedMethods().contains(requestMethod.name());
                        if (methodAllowed) {
                            response.getHeaders().add("Access-Control-Allow-Methods", String.join(",", config.getAllowedMethods()));
                        }
                    }
                    
                    List<String> requestHeaders = request.getHeaders().getAccessControlRequestHeaders();
                    if (requestHeaders != null && !requestHeaders.isEmpty()) {
                        response.getHeaders().add("Access-Control-Allow-Headers", String.join(",", requestHeaders));
                    }
                    
                    response.getHeaders().add("Access-Control-Max-Age", "3600");
                    response.setStatusCode(HttpStatus.OK);
                    return Mono.empty();
                }
            }
            return chain.filter(exchange);
        };
    }

    static class CorsConfigDto {
        private String allowedOrigins;
        private String allowedMethods;
        private String allowedHeaders;
        
        public String getAllowedOrigins() { return allowedOrigins; }
        public void setAllowedOrigins(String allowedOrigins) { this.allowedOrigins = allowedOrigins; }
        public String getAllowedMethods() { return allowedMethods; }
        public void setAllowedMethods(String allowedMethods) { this.allowedMethods = allowedMethods; }
        public String getAllowedHeaders() { return allowedHeaders; }
        public void setAllowedHeaders(String allowedHeaders) { this.allowedHeaders = allowedHeaders; }
    }
}
