package com.example.cinema.catalog.infrastructure.database.repositories;

import com.example.cinema.catalog.infrastructure.database.entities.GenreJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataGenreRepository extends JpaRepository<GenreJpaEntity, String> {
}
