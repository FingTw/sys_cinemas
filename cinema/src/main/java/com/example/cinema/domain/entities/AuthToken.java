package com.example.cinema.domain.entities;

import java.time.ZonedDateTime;

public class AuthToken {

    private String id;
    private String userId;
    private String tokenJti;
    private String tokenHash;
    private String ipAddress;
    private String userAgent;
    private boolean isActive;
    private ZonedDateTime issuedAt;
    private ZonedDateTime expiresAt;
    private ZonedDateTime revokedAt;

    public AuthToken() {
    }

    public AuthToken(String id, String userId, String tokenJti, String tokenHash, String ipAddress, String userAgent, boolean isActive, ZonedDateTime issuedAt, ZonedDateTime expiresAt, ZonedDateTime revokedAt) {
        this.id = id;
        this.userId = userId;
        this.tokenJti = tokenJti;
        this.tokenHash = tokenHash;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.isActive = isActive;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
    }

    public static AuthTokenBuilder builder() {
        return new AuthTokenBuilder();
    }

    public static class AuthTokenBuilder {
        private String id;
        private String userId;
        private String tokenJti;
        private String tokenHash;
        private String ipAddress;
        private String userAgent;
        private boolean isActive = true;
        private ZonedDateTime issuedAt;
        private ZonedDateTime expiresAt;
        private ZonedDateTime revokedAt;

        public AuthTokenBuilder id(String id) {
            this.id = id;
            return this;
        }

        public AuthTokenBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public AuthTokenBuilder tokenJti(String tokenJti) {
            this.tokenJti = tokenJti;
            return this;
        }

        public AuthTokenBuilder tokenHash(String tokenHash) {
            this.tokenHash = tokenHash;
            return this;
        }

        public AuthTokenBuilder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public AuthTokenBuilder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public AuthTokenBuilder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public AuthTokenBuilder issuedAt(ZonedDateTime issuedAt) {
            this.issuedAt = issuedAt;
            return this;
        }

        public AuthTokenBuilder expiresAt(ZonedDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public AuthTokenBuilder revokedAt(ZonedDateTime revokedAt) {
            this.revokedAt = revokedAt;
            return this;
        }

        public AuthToken build() {
            return new AuthToken(id, userId, tokenJti, tokenHash, ipAddress, userAgent, isActive, issuedAt, expiresAt, revokedAt);
        }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTokenJti() { return tokenJti; }
    public void setTokenJti(String tokenJti) { this.tokenJti = tokenJti; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public ZonedDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(ZonedDateTime issuedAt) { this.issuedAt = issuedAt; }
    public ZonedDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(ZonedDateTime expiresAt) { this.expiresAt = expiresAt; }
    public ZonedDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(ZonedDateTime revokedAt) { this.revokedAt = revokedAt; }
}
