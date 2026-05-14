package com.example.cinema.infrastructure.database.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Date;

@Entity
@Table(name = "token_blacklist", schema = "auth")
public class TokenBlacklist {

    @Id
    @Column(length = 1000)
    private String token;

    @Column(nullable = false)
    private Date expiresAt;

    public TokenBlacklist() {
    }

    public TokenBlacklist(String token, Date expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }

    // Builder manual
    public static TokenBlacklistBuilder builder() {
        return new TokenBlacklistBuilder();
    }

    public static class TokenBlacklistBuilder {
        private String token;
        private Date expiresAt;

        public TokenBlacklistBuilder token(String token) {
            this.token = token;
            return this;
        }

        public TokenBlacklistBuilder expiresAt(Date expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public TokenBlacklist build() {
            return new TokenBlacklist(token, expiresAt);
        }
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }
}
