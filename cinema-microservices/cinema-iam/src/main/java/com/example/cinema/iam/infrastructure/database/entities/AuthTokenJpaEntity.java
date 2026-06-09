package com.example.cinema.iam.infrastructure.database.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import java.time.ZonedDateTime;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import lombok.*;

@Entity
@Table(name = "auth_tokens", schema = "auth")
@SQLDelete(sql = "UPDATE auth.auth_tokens SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
public class AuthTokenJpaEntity {
    @jakarta.persistence.Column(name = "is_deleted")
    private boolean isDeleted = false;

    @Id
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

    @Builder
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
}
