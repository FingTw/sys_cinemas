package com.example.cinema.admin.repositories;

import com.example.cinema.admin.entities.CorsConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CorsConfigRepository extends JpaRepository<CorsConfig, String> {
}
