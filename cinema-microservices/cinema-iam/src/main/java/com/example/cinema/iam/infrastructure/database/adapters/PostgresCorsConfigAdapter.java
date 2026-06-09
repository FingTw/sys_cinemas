package com.example.cinema.iam.infrastructure.database.adapters;

import com.example.cinema.iam.domain.entities.CorsConfig;
import com.example.cinema.iam.domain.repositories.CorsConfigRepository;
import com.example.cinema.iam.infrastructure.database.entities.CorsConfigJpaEntity;
import com.example.cinema.iam.infrastructure.database.repositories.SpringDataCorsConfigRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PostgresCorsConfigAdapter implements CorsConfigRepository {

    private final SpringDataCorsConfigRepository repository;

    public PostgresCorsConfigAdapter(SpringDataCorsConfigRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<CorsConfig> findById(String id) {
        return repository.findById(id).map(this::mapToDomain);
    }

    @Override
    public void save(CorsConfig config) {
        repository.save(mapToJpa(config));
    }

    private CorsConfig mapToDomain(CorsConfigJpaEntity entity) {
        return CorsConfig.builder()
                .id(entity.getId())
                .allowedOrigins(entity.getAllowedOrigins())
                .allowedMethods(entity.getAllowedMethods())
                .allowedHeaders(entity.getAllowedHeaders())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private CorsConfigJpaEntity mapToJpa(CorsConfig config) {
        return CorsConfigJpaEntity.builder()
                .id(config.getId())
                .allowedOrigins(config.getAllowedOrigins())
                .allowedMethods(config.getAllowedMethods())
                .allowedHeaders(config.getAllowedHeaders())
                .updatedAt(config.getUpdatedAt())
                .build();
    }
}
