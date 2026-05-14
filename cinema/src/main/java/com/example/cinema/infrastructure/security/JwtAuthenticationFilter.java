package com.example.cinema.infrastructure.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.cinema.application.ports.in.ValidateTokenUseCase;
import com.example.cinema.application.ports.out.TokenBlacklistPort;
import com.example.cinema.application.ports.out.TokenCachePort;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistPort tokenBlacklistPort;
    private final ValidateTokenUseCase validateTokenUseCase;
    private final TokenCachePort tokenCachePort;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, TokenBlacklistPort tokenBlacklistPort, ValidateTokenUseCase validateTokenUseCase, TokenCachePort tokenCachePort) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenBlacklistPort = tokenBlacklistPort;
        this.validateTokenUseCase = validateTokenUseCase;
        this.tokenCachePort = tokenCachePort;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                // FAST PATH: Check Redis Cache first
                boolean isFastValid = tokenCachePort.isValidToken(jwt);

                if (!isFastValid) {
                    // FULL PATH: Check Blacklist and Version (hits DB)
                    if (tokenBlacklistPort.isBlacklisted(jwt)) {
                        log.warn("WARNING: Detected access using blacklisted token!");
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"error\": \"Token has been blacklisted (Session terminated)\"}");
                        return;
                    }

                    String userId = jwtTokenProvider.getUserIdFromToken(jwt);
                    Long tokenVersion = jwtTokenProvider.getVersionFromToken(jwt);

                    if (userId != null && tokenVersion != null && !validateTokenUseCase.isValidVersion(userId, tokenVersion)) {
                        log.warn("Token version mismatch for userId: {}", userId);
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"error\": \"Token revoked or expired (Version mismatch)\"}");
                        return;
                    }

                    // If we reached here, it's valid. CACHE it for next time.
                    tokenCachePort.cacheToken(jwt, userId, 600000); // 10 mins
                }

                // Token is valid (either via fast path or full path)
                String username = jwtTokenProvider.getUsernameFromToken(jwt);
                String rolesString = jwtTokenProvider.getRolesFromToken(jwt);
                String permissionsString = jwtTokenProvider.getPermissionsFromToken(jwt);

                // Set Authentication in Context
                java.util.List<org.springframework.security.core.authority.SimpleGrantedAuthority> authorities = java.util.stream.Stream.concat(
                        java.util.Optional.ofNullable(rolesString).stream()
                                .flatMap(value -> java.util.Arrays.stream(value.split(","))),
                        java.util.Optional.ofNullable(permissionsString).stream()
                                .flatMap(value -> java.util.Arrays.stream(value.split(","))))
                        .filter(org.springframework.util.StringUtils::hasText)
                        .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                        .distinct()
                        .collect(java.util.stream.Collectors.toList());

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (java.io.IOException | RuntimeException ex) {
            log.error("Error authenticating token in security context: {}", ex.getMessage(), ex);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            String json = String.format("{\"timestamp\": \"%s\", \"status\": 401, \"error\": \"Unauthorized\", \"message\": \"Thong tin xac thuc khong hop le hoac he thong xac thuc dang gap su co.\"}", java.time.ZonedDateTime.now().toString());
            response.getWriter().write(json);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
