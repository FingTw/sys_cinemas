package com.example.cinema.admin.infrastructure.database.adapters;

import com.example.cinema.admin.domain.entities.CorsConfig;
import com.example.cinema.admin.domain.repositories.CorsConfigRepository;
import com.example.cinema.admin.infrastructure.database.entities.CorsConfigJpaEntity;
import com.example.cinema.admin.infrastructure.database.repositories.SpringDataCorsConfigRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class CorsConfigRepositoryAdapter implements CorsConfigRepository {

    private final SpringDataCorsConfigRepository springDataCorsConfigRepository;

    public CorsConfigRepositoryAdapter(SpringDataCorsConfigRepository springDataCorsConfigRepository) {
        this.springDataCorsConfigRepository = springDataCorsConfigRepository;
    }

    @Override
    public CorsConfig save(CorsConfig corsConfig) {
        CorsConfigJpaEntity entity = toEntity(corsConfig);
        CorsConfigJpaEntity saved = springDataCorsConfigRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<CorsConfig> findById(String id) {
        return springDataCorsConfigRepository.findById(id).map(this::toDomain);
    }

    private CorsConfig toDomain(CorsConfigJpaEntity entity) {
        return CorsConfig.builder()
                .id(entity.getId())
                .allowedOrigins(entity.getAllowedOrigins())
                .allowedMethods(entity.getAllowedMethods())
                .allowedHeaders(entity.getAllowedHeaders())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private CorsConfigJpaEntity toEntity(CorsConfig domain) {
        return CorsConfigJpaEntity.builder()
                .id(domain.getId())
                .allowedOrigins(domain.getAllowedOrigins())
                .allowedMethods(domain.getAllowedMethods())
                .allowedHeaders(domain.getAllowedHeaders())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
