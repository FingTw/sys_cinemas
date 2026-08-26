package com.example.cinema.admin.repositories;

import com.example.cinema.admin.entities.FeaturedMovie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeaturedMovieRepository extends JpaRepository<FeaturedMovie, String> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"movie"})
    List<FeaturedMovie> findAllByOrderByDisplayOrderAsc();

    boolean existsByMovieId(String movieId);
}
