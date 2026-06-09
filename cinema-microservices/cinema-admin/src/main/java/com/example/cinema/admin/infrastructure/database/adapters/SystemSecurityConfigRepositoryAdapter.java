package com.example.cinema.admin.infrastructure.database.adapters;

import com.example.cinema.admin.domain.entities.SystemSecurityConfig;
import com.example.cinema.admin.domain.repositories.SystemSecurityConfigRepository;
import com.example.cinema.admin.infrastructure.database.entities.SystemSecurityConfigJpaEntity;
import com.example.cinema.admin.infrastructure.database.repositories.SpringDataSystemSecurityConfigRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class SystemSecurityConfigRepositoryAdapter implements SystemSecurityConfigRepository {

    private final SpringDataSystemSecurityConfigRepository springDataSystemSecurityConfigRepository;

    public SystemSecurityConfigRepositoryAdapter(SpringDataSystemSecurityConfigRepository springDataSystemSecurityConfigRepository) {
        this.springDataSystemSecurityConfigRepository = springDataSystemSecurityConfigRepository;
    }

    @Override
    public SystemSecurityConfig save(SystemSecurityConfig config) {
        SystemSecurityConfigJpaEntity entity = toEntity(config);
        SystemSecurityConfigJpaEntity saved = springDataSystemSecurityConfigRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<SystemSecurityConfig> findById(String id) {
        return springDataSystemSecurityConfigRepository.findById(id).map(this::toDomain);
    }

    private SystemSecurityConfig toDomain(SystemSecurityConfigJpaEntity entity) {
        return SystemSecurityConfig.builder()
                .id(entity.getId())
                .clientKey(entity.getClientKey())
                .gatewayProtectedPaths(entity.getGatewayProtectedPaths())
                .serviceBypassPaths(entity.getServiceBypassPaths())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private SystemSecurityConfigJpaEntity toEntity(SystemSecurityConfig domain) {
        return SystemSecurityConfigJpaEntity.builder()
                .id(domain.getId())
                .clientKey(domain.getClientKey())
                .gatewayProtectedPaths(domain.getGatewayProtectedPaths())
                .serviceBypassPaths(domain.getServiceBypassPaths())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
