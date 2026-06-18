package com.example.cinema.admin.infrastructure.database.repositories;

import com.example.cinema.admin.infrastructure.database.entities.CinemaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SpringDataCinemaRepository extends JpaRepository<CinemaJpaEntity, String> {
    List<CinemaJpaEntity> findByComplexId(String complexId);
}
