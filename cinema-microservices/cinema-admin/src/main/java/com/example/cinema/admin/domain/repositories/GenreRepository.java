package com.example.cinema.admin.domain.repositories;

import com.example.cinema.admin.domain.entities.Genre;
import java.util.List;
import java.util.Optional;

public interface GenreRepository {
    Genre save(Genre genre);
    Optional<Genre> findById(String id);
    Optional<Genre> findByCode(String code);
    List<Genre> findAll();
    void deleteById(String id);
    List<Genre> findAllByIds(List<String> ids);
}
