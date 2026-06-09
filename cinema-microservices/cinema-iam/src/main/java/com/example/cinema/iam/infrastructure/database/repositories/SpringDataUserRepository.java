package com.example.cinema.iam.infrastructure.database.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.cinema.iam.infrastructure.database.entities.UserJpaEntity;

// File này tận dụng sức mạnh của Spring Data JPA
@Repository
public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, String> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<UserJpaEntity> findByUsername(String username);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<UserJpaEntity> findById(String id);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @org.springframework.data.jpa.repository.Query("SELECT u.tokenVersion FROM UserJpaEntity u WHERE u.id = :id")
    Long findTokenVersionById(@org.springframework.data.repository.query.Param("id") String id);
}
