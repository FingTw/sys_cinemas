package com.example.cinema.admin.infrastructure.database.repositories;

import com.example.cinema.admin.infrastructure.database.entities.GenreJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SpringDataGenreRepository extends JpaRepository<GenreJpaEntity, String> {
    Optional<GenreJpaEntity> findByCode(String code);
}
