package com.example.cinema.application.ports.out;

public interface TokenCachePort {
    Long getUserTokenVersion(String userId);
    void incrementTokenVersion(String userId);
    
    // Caching the JWT itself for super-fast validation
    void cacheToken(String token, String userId, long expirationMs);
    boolean isValidToken(String token);
    void removeToken(String token);
    
    // Caching user roles and permissions for fast authorization checks
    void cacheUserRoles(String userId, String roles);
    String getUserRoles(String userId);
    
    void cacheUserPermissions(String userId, String permissions);
    String getUserPermissions(String userId);
}
