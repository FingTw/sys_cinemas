package com.example.cinema.iam.infrastructure.database.repositories;

import com.example.cinema.iam.infrastructure.database.entities.SsoRoleMappingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataSsoRoleMappingRepository extends JpaRepository<SsoRoleMappingJpaEntity, UUID> {
    Optional<SsoRoleMappingJpaEntity> findBySsoRoleName(String ssoRoleName);
}
