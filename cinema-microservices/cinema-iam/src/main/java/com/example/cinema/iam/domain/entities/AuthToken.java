package com.example.cinema.iam.domain.entities;

import java.time.ZonedDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
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

    @Builder
    public AuthToken(String id, String userId, String tokenJti, String tokenHash, String ipAddress, String userAgent, boolean isActive, ZonedDateTime issuedAt, ZonedDateTime expiresAt, ZonedDateTime revokedAt) {
        this.id = (id != null && !id.trim().isEmpty()) ? id : java.util.UUID.randomUUID().toString();
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
}
