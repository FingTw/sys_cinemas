package com.example.cinema.common.security;

import com.example.cinema.common.exception.AuthException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Cung cấp các thao tác với JWT: tạo token, validate, trích xuất claims.
 *
 * Khi token không hợp lệ, ném ra AuthException thay vì trả về boolean.
 * Điều này đảm bảo lỗi luôn được xử lý rõ ràng ở tầng trên.
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.security.jwt-secret}")
    private String jwtSecret;

    @Value("${app.security.jwt-expiration-ms}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(String username, String userId, String roles, String permissions) {
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("roles", roles)
                .claim("permissions", permissions)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Validate JWT. Ném AuthException nếu token không hợp lệ.
     * Không có trường hợp trả về false — luôn thành công hoặc ném exception.
     */
    public void validateToken(String authToken) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(authToken);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("Token expired for subject: {}", extractSubjectSafely(e));
            throw AuthException.tokenExpired();
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            log.warn("Invalid token: {}", e.getClass().getSimpleName());
            throw AuthException.tokenInvalid();
        }
    }

    /**
     * Kiểm tra nhanh token có hợp lệ không (không ném exception).
     */
    public boolean isValid(String authToken) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(authToken);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public String getUserIdFromToken(String token) {
        return parseClaims(token).get("userId", String.class);
    }

    public String getRolesFromToken(String token) {
        return parseClaims(token).get("roles", String.class);
    }

    public String getPermissionsFromToken(String token) {
        return parseClaims(token).get("permissions", String.class);
    }



    public Date getExpirationDateFromToken(String token) {
        return parseClaims(token).getExpiration();
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private io.jsonwebtoken.Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build()
                .parseSignedClaims(token).getPayload();
    }

    private String extractSubjectSafely(io.jsonwebtoken.ExpiredJwtException e) {
        try {
            return e.getClaims().getSubject();
        } catch (Exception ignored) {
            return "unknown";
        }
    }
}
