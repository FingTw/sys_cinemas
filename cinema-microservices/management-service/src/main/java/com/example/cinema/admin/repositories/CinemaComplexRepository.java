package com.example.cinema.admin.repositories;

import com.example.cinema.admin.entities.CinemaComplex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CinemaComplexRepository extends JpaRepository<CinemaComplex, String> {
}
