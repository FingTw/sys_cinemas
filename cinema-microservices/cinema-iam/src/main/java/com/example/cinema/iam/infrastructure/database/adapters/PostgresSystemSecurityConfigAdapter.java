package com.example.cinema.iam.infrastructure.database.adapters;

import com.example.cinema.iam.domain.entities.SystemSecurityConfig;
import com.example.cinema.iam.domain.repositories.SystemSecurityConfigRepository;
import com.example.cinema.iam.infrastructure.database.entities.SystemSecurityConfigJpaEntity;
import com.example.cinema.iam.infrastructure.database.repositories.SpringDataSystemSecurityConfigRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PostgresSystemSecurityConfigAdapter implements SystemSecurityConfigRepository {

    private final SpringDataSystemSecurityConfigRepository repository;

    public PostgresSystemSecurityConfigAdapter(SpringDataSystemSecurityConfigRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<SystemSecurityConfig> findById(String id) {
        return repository.findById(id).map(this::mapToDomain);
    }

    @Override
    public void save(SystemSecurityConfig config) {
        repository.save(mapToJpa(config));
    }

    private SystemSecurityConfig mapToDomain(SystemSecurityConfigJpaEntity entity) {
        return SystemSecurityConfig.builder()
                .id(entity.getId())
                .clientKey(entity.getClientKey())
                .gatewayProtectedPaths(entity.getGatewayProtectedPaths())
                .serviceBypassPaths(entity.getServiceBypassPaths())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private SystemSecurityConfigJpaEntity mapToJpa(SystemSecurityConfig config) {
        return SystemSecurityConfigJpaEntity.builder()
                .id(config.getId())
                .clientKey(config.getClientKey())
                .gatewayProtectedPaths(config.getGatewayProtectedPaths())
                .serviceBypassPaths(config.getServiceBypassPaths())
                .updatedAt(config.getUpdatedAt())
                .build();
    }
}
