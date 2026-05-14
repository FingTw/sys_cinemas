package com.example.cinema.infrastructure.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

    // Khai báo secret key dài ít nhất 32 ký tự trong application.yml
    @Value("${app.security.jwt-secret}")
    private String jwtSecret;

    @Value("${app.security.jwt-expiration-ms}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(String username, java.util.Collection<String> roles, java.util.Collection<String> permissions, String userId, Long tokenVersion) {
        String rolesString = roles.stream()
                .map(role -> "ROLE_" + role)
                .collect(java.util.stream.Collectors.joining(","));

        String permissionsString = String.join(",", permissions);

        return Jwts.builder()
                .subject(username)
                .claim("roles", rolesString)
                .claim("permissions", permissionsString)
                .claim("userId", userId)
                .claim("tokenVersion", tokenVersion)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String getPermissionsFromToken(String token) {
        Object permissions = Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload().get("permissions");
        if (permissions instanceof String permissionString) {
            return permissionString;
        }
        return permissions != null ? permissions.toString() : null;
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(authToken);
            return true;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            System.err.println("Token expired: " + e.getMessage());
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            System.err.println("Token invalid: " + e.getMessage());
        }
        return false;
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload().getSubject();
    }

    public String getRolesFromToken(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload().get("roles", String.class);
    }

    public String getUserIdFromToken(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload().get("userId", String.class);
    }

    public Long getVersionFromToken(String token) {
        Object version = Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload().get("tokenVersion");
        if (version instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    public Date getExpirationDateFromToken(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload().getExpiration();
    }
}
