package com.example.cinema.admin.repositories;

import com.example.cinema.admin.entities.Cinema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CinemaRepository extends JpaRepository<Cinema, String> {
    List<Cinema> findByComplexId(String complexId);
}
