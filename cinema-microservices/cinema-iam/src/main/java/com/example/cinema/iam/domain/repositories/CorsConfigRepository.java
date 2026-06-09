package com.example.cinema.iam.domain.repositories;

import com.example.cinema.iam.domain.entities.CorsConfig;
import java.util.Optional;

public interface CorsConfigRepository {
    Optional<CorsConfig> findById(String id);
    void save(CorsConfig config);
}
