package com.example.cinema.admin.infrastructure.database.repositories;

import com.example.cinema.admin.infrastructure.database.entities.AuthTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthTokenJpaEntity, UUID> {
    List<AuthTokenJpaEntity> findAllByOrderByIssuedAtDesc();
}
