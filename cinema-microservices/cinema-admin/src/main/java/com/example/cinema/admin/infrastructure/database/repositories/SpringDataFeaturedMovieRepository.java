package com.example.cinema.admin.infrastructure.database.repositories;

import com.example.cinema.admin.infrastructure.database.entities.FeaturedMovieJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataFeaturedMovieRepository extends JpaRepository<FeaturedMovieJpaEntity, String> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"movie"})
    List<FeaturedMovieJpaEntity> findAllByOrderByDisplayOrderAsc();

    boolean existsByMovieId(String movieId);
}
