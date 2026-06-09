package com.example.cinema.admin.infrastructure.database.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_policies", schema = "auth")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordPolicyJpaEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "min_length")
    private int minLength;

    @Column(name = "require_uppercase")
    private boolean requireUppercase;

    @Column(name = "require_lowercase")
    private boolean requireLowercase;

    @Column(name = "require_number")
    private boolean requireNumber;

    @Column(name = "require_special_char")
    private boolean requireSpecialChar;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
