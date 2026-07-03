package com.example.cinema.admin.infrastructure.database.repositories;

import com.example.cinema.admin.infrastructure.database.entities.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, String> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<UserJpaEntity> findByUsername(String username);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<UserJpaEntity> findById(String id);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE UserJpaEntity u SET u.cinemaId = :cinemaId WHERE u.id = :id")
    void updateWorkplace(@org.springframework.data.repository.query.Param("id") String id, @org.springframework.data.repository.query.Param("cinemaId") String cinemaId);
}
