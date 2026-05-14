package com.example.cinema.iam.infrastructure.database.repositories;

import com.example.cinema.iam.infrastructure.database.entities.PermissionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataPermissionRepository extends JpaRepository<PermissionJpaEntity, UUID> {
    Optional<PermissionJpaEntity> findByName(String name);
}
