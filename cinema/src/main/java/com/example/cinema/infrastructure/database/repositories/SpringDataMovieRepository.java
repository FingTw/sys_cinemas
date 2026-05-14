package com.example.cinema.infrastructure.database.repositories;

import com.example.cinema.infrastructure.database.entities.MovieJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataMovieRepository extends JpaRepository<MovieJpaEntity, String> {
}
