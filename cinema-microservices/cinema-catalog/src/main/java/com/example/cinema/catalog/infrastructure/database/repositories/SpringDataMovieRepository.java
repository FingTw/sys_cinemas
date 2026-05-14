package com.example.cinema.catalog.infrastructure.database.repositories;

import com.example.cinema.catalog.infrastructure.database.entities.MovieJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataMovieRepository extends JpaRepository<MovieJpaEntity, String> {
}
