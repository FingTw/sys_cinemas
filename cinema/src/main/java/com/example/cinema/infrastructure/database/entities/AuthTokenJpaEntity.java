package com.example.cinema.infrastructure.database.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;
import java.time.ZonedDateTime;

@Entity
@Table(name = "auth_tokens", schema = "auth")
public class AuthTokenJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String userId;

    @Column(unique = true, nullable = false)
    private String tokenJti;

    @Column(nullable = false, length = 512)
    private String tokenHash;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 512)
    private String userAgent;

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(nullable = false)
    private ZonedDateTime issuedAt;

    @Column(nullable = false)
    private ZonedDateTime expiresAt;

    @Column
    private ZonedDateTime revokedAt;

    public AuthTokenJpaEntity() {
    }

    public AuthTokenJpaEntity(UUID id, String userId, String tokenJti, String tokenHash, String ipAddress, String userAgent, boolean isActive, ZonedDateTime issuedAt, ZonedDateTime expiresAt, ZonedDateTime revokedAt) {
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

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
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
