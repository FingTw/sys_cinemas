package com.example.cinema.admin.domain.repositories;

import com.example.cinema.admin.domain.entities.CorsConfig;
import java.util.Optional;

public interface CorsConfigRepository {
    CorsConfig save(CorsConfig corsConfig);
    Optional<CorsConfig> findById(String id);
}
