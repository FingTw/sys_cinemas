package com.example.cinema.admin.domain.repositories;

import com.example.cinema.admin.domain.entities.Cinema;
import java.util.List;
import java.util.Optional;

public interface CinemaRepository {
    Cinema save(Cinema cinema);
    Optional<Cinema> findById(String id);
    List<Cinema> findAll();
    List<Cinema> findByComplexId(String complexId);
    void deleteById(String id);
}
