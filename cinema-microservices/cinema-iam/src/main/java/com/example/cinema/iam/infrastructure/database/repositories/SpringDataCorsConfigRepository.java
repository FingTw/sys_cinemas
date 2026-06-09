package com.example.cinema.iam.infrastructure.database.repositories;

import com.example.cinema.iam.infrastructure.database.entities.CorsConfigJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataCorsConfigRepository extends JpaRepository<CorsConfigJpaEntity, String> {
}
