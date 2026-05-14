package com.example.cinema.iam.infrastructure.database.repositories;

import com.example.cinema.iam.infrastructure.database.entities.AuthTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;

@Repository
public interface SpringDataTokenRepository extends JpaRepository<AuthTokenJpaEntity, java.util.UUID> {

    @Modifying
    @Query("UPDATE AuthTokenJpaEntity t SET t.isActive = false, t.revokedAt = :now WHERE t.userId = :userId AND t.isActive = true")
    void revokeAllByUserId(@Param("userId") String userId, @Param("now") ZonedDateTime now);

    @Modifying
    @Query("UPDATE AuthTokenJpaEntity t SET t.isActive = false, t.revokedAt = :now WHERE t.tokenJti = :jti AND t.isActive = true")
    void revokeByJti(@Param("jti") String jti, @Param("now") ZonedDateTime now);

    java.util.Optional<AuthTokenJpaEntity> findByTokenJti(String tokenJti);
    java.util.List<AuthTokenJpaEntity> findAllByUserId(String userId);
}
