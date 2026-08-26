package com.example.cinema.auth.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users", schema = "auth")
@SQLDelete(sql = "UPDATE auth.users SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class User {
    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private boolean isBlocked = false;

    @Column(name = "sso_subject", unique = true)
    private String ssoSubject;

    @Column(name = "cinema_id")
    private String cinemaId;

    public User() {
    }

    public User(String id, String username, String email, boolean isBlocked, String ssoSubject, String cinemaId) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.isBlocked = isBlocked;
        this.ssoSubject = ssoSubject;
        this.cinemaId = cinemaId;
    }

    public void block() {
        this.isBlocked = true;
    }

    public void unblock() {
        this.isBlocked = false;
    }

    public void updateEmail(String newEmail) {
        this.email = newEmail;
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static class UserBuilder {
        private String id;
        private String username;
        private String email;
        private boolean isBlocked = false;
        private String ssoSubject;
        private String cinemaId;

        public UserBuilder id(String id) {
            this.id = id;
            return this;
        }

        public UserBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder isBlocked(boolean isBlocked) {
            this.isBlocked = isBlocked;
            return this;
        }

        public UserBuilder ssoSubject(String ssoSubject) {
            this.ssoSubject = ssoSubject;
            return this;
        }

        public UserBuilder cinemaId(String cinemaId) {
            this.cinemaId = cinemaId;
            return this;
        }

        public User build() {
            return new User(id, username, email, isBlocked, ssoSubject, cinemaId);
        }
    }
}
