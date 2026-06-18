package com.example.cinema.admin.domain.repositories;

import com.example.cinema.admin.domain.entities.CinemaComplex;
import java.util.List;
import java.util.Optional;

public interface CinemaComplexRepository {
    CinemaComplex save(CinemaComplex complex);
    Optional<CinemaComplex> findById(String id);
    List<CinemaComplex> findAll();
    void deleteById(String id);
}
