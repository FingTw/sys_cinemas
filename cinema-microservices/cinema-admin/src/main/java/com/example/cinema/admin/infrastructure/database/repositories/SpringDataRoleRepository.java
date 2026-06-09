package com.example.cinema.admin.infrastructure.database.repositories;

import com.example.cinema.admin.infrastructure.database.entities.RoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataRoleRepository extends JpaRepository<RoleJpaEntity, UUID> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"permissions"})
    Optional<RoleJpaEntity> findByName(String name);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"permissions"})
    Optional<RoleJpaEntity> findById(UUID id);
}
