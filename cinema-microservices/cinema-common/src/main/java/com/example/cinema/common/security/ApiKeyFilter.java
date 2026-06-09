package com.example.cinema.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.data.redis.core.StringRedisTemplate;
import java.io.IOException;
import java.time.ZonedDateTime;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    @Value("${app.security.api-key}")
    private String validApiKey;

    private final StringRedisTemplate redisTemplate;

    public ApiKeyFilter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // Always bypass internal APIs (they are verified at the controller level using X-Internal-Api-Key)
        if (path.startsWith("/api/v1/internal/")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Load bypass paths from Redis or use fallback static list
        String bypassPathsStr = null;
        try {
            bypassPathsStr = redisTemplate.opsForValue().get("security:bypass-paths");
        } catch (Exception e) {
            // Log or ignore Redis connectivity issues, fallback to default behavior
        }

        boolean isBypassed = false;
        if (bypassPathsStr != null && !bypassPathsStr.trim().isEmpty()) {
            String[] paths = bypassPathsStr.split(",");
            for (String p : paths) {
                String trimmed = p.trim();
                if (!trimmed.isEmpty()) {
                    // Match prefix or exact paths
                    if (path.startsWith(trimmed) || path.equals(trimmed)) {
                        isBypassed = true;
                        break;
                    }
                }
            }
        } else {
            // Fallback to static check list
            isBypassed = path.startsWith("/actuator")
                    || path.startsWith("/v3/api-docs")
                    || path.startsWith("/swagger-ui")
                    || path.equals("/api/v1/auth/public-key")
                    || path.startsWith("/api/v1/movies")
                    || path.startsWith("/api/v1/featured-movies")
                    || path.startsWith("/api/v1/showtimes")
                    || path.startsWith("/api/v1/rooms")
                    || path.startsWith("/api/v1/vnpay");
        }

        if (isBypassed) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader("X-API-Key");
        String authHeader = request.getHeader("Authorization");

        // Nếu có Authorization (JWT) thì bỏ qua check API Key vì JWTAuthenticationFilter sẽ lo
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Get expected API Key from Redis or fallback to config properties
        String currentApiKey = validApiKey;
        try {
            String cachedKey = redisTemplate.opsForValue().get("security:client-key");
            if (cachedKey != null && !cachedKey.trim().isEmpty()) {
                currentApiKey = cachedKey;
            }
        } catch (Exception e) {
            // Fallback to validApiKey
        }

        // Nếu không có JWT, bắt buộc phải có API Key hợp lệ
        if (apiKey == null || !apiKey.equals(currentApiKey)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            String json = String.format("{\"timestamp\": \"%s\", \"status\": 403, \"error\": \"Forbidden\", \"message\": \"Thieu hoac sai X-API-Key\"}", ZonedDateTime.now().toString());
            response.getWriter().write(json);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
