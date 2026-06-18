package com.example.cinema.common.security;

import com.example.cinema.common.exception.AuthException;
import com.example.cinema.common.handler.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Filter xác thực JWT — chạy một lần mỗi request.
 *
 * Luồng xử lý:
 *  1. Trích xuất Bearer token từ header Authorization
 *  2. Validate token (ném AuthException nếu hết hạn/không hợp lệ)
 *  3. Kiểm tra Redis blacklist (token đã logout)
 *  4. Kiểm tra Single-Session (token đang active của user)
 *  5. Set Authentication vào SecurityContext
 *
 * Mọi AuthException đều được bắt tại đây và trả về JSON chuẩn
 * qua writeAuthErrorResponse() — không để lọt lên Spring Security's default 403.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, StringRedisTemplate redisTemplate) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = extractToken(request);

        if (!StringUtils.hasText(jwt)) {
            // Không có token — tiếp tục, để Spring Security quyết định nếu endpoint cần auth
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Step 1: Validate chữ ký và thời hạn (ném AuthException nếu lỗi)
            jwtTokenProvider.validateToken(jwt);

            // Step 2: Kiểm tra Redis blacklist (token đã bị thu hồi do logout)
            if (Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + jwt))) {
                log.warn("[SECURITY] Blacklisted token used | IP: {}", request.getRemoteAddr());
                throw AuthException.tokenBlacklisted();
            }

            // Step 3: Kiểm tra Single-Session — đảm bảo token này là token đang active
            String userId = jwtTokenProvider.getUserIdFromToken(jwt);
            String activeToken = redisTemplate.opsForValue().get("valid_token:" + userId);
            if (activeToken != null && !jwt.equals(activeToken)) {
                log.warn("[SECURITY] Stale token used for UserID: {}", userId);
                throw AuthException.sessionInvalidated(userId);
            }

            // Step 4: Trích xuất authorities từ Redis thay vì JWT
            String username = jwtTokenProvider.getUsernameFromToken(jwt);
            String authContext = redisTemplate.opsForValue().get("user_auth:" + userId);
            
            String rolesStr = "";
            String permissionsStr = "";
            String cinemaId = null;
            
            if (StringUtils.hasText(authContext)) {
                String[] parts = authContext.split("\\|");
                if (parts.length > 0) rolesStr = parts[0];
                if (parts.length > 1) permissionsStr = parts[1];
                if (parts.length > 2) cinemaId = parts[2];
            }

            List<SimpleGrantedAuthority> authorities = Stream.concat(
                    Optional.ofNullable(rolesStr).filter(StringUtils::hasText).stream().flatMap(s -> Arrays.stream(s.split(",")))
                            .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r),
                    Optional.ofNullable(permissionsStr).filter(StringUtils::hasText).stream().flatMap(s -> Arrays.stream(s.split(",")))
            )
                    .filter(StringUtils::hasText)
                    .distinct()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
            
            if (StringUtils.hasText(cinemaId)) {
                final String finalCinemaId = cinemaId;
                // Đưa cinemaId vào mục Details của Authentication
                org.springframework.security.web.authentication.WebAuthenticationDetails details = 
                    new org.springframework.security.web.authentication.WebAuthenticationDetails(request) {
                        @Override
                        public String getSessionId() {
                            return finalCinemaId; // Tái sử dụng trường sessionId để lưu cinemaId tạm thời
                        }
                    };
                authentication.setDetails(details);
            }

            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (AuthException ex) {
            // Tất cả lỗi xác thực đều được xử lý tập trung ở đây
            writeAuthErrorResponse(response, request, ex);
        }
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void writeAuthErrorResponse(HttpServletResponse response, HttpServletRequest request,
                                        AuthException ex) throws IOException {
        log.warn("[AUTH] {} | {} {} | {}", ex.getErrorCode(), request.getMethod(), request.getRequestURI(), ex.getMessage());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(ZonedDateTime.now())
                .status(HttpServletResponse.SC_UNAUTHORIZED)
                .error("Unauthorized")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .service(ex.getServiceName())
                .errorCode(ex.getErrorCode())
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
