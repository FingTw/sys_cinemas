package com.example.cinema.admin.entities;

import jakarta.persistence.Column;
import jakarta.persistence.*;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import java.util.UUID;
import java.time.ZonedDateTime;
import org.hibernate.annotations.SQLRestriction;
import lombok.*;

@Entity
@Table(name = "auth_tokens", schema = "auth")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthToken {
    @Builder.Default
    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(unique = true, nullable = false)
    private String tokenJti;

    @Column(nullable = false, length = 512)
    private String tokenHash;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 512)
    private String userAgent;

    @Builder.Default
    @Column(nullable = false)
    private boolean isActive = true;

    @Column(nullable = false)
    private ZonedDateTime issuedAt;

    @Column(nullable = false)
    private ZonedDateTime expiresAt;

    @Column
    private ZonedDateTime revokedAt;
}
