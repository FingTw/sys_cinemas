package com.example.cinema.catalog.domain.repositories;

import com.example.cinema.catalog.domain.entities.Genre;
import java.util.List;
import java.util.Optional;

public interface GenreRepository {
    Optional<Genre> findById(String id);
    List<Genre> findAll();
}
