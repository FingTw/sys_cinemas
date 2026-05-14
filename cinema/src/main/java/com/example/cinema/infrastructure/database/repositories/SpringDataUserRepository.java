package com.example.cinema.infrastructure.database.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.cinema.infrastructure.database.entities.UserJpaEntity;

// File này tận dụng sức mạnh của Spring Data JPA
@Repository
public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, String> {

    Optional<UserJpaEntity> findByUsername(String username);

    boolean existsByUsername(String username);

    @org.springframework.data.jpa.repository.Query("SELECT u.tokenVersion FROM UserJpaEntity u WHERE u.id = :id")
    Long findTokenVersionById(@org.springframework.data.repository.query.Param("id") String id);
}
