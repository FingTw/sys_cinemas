package com.example.cinema.admin.domain.repositories;

import com.example.cinema.admin.domain.entities.Movie;
import java.util.List;
import java.util.Optional;

public interface MovieRepository {
    Movie save(Movie movie);
    Optional<Movie> findById(String id);
    List<Movie> findAll();
    void deleteById(String id);
}
