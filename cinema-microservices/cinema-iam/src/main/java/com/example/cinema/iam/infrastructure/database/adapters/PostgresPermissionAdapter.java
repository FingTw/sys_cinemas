package com.example.cinema.iam.infrastructure.database.adapters;

import com.example.cinema.iam.domain.repositories.PermissionRepository;
import com.example.cinema.iam.domain.entities.Permission;
import com.example.cinema.iam.infrastructure.database.entities.PermissionJpaEntity;
import com.example.cinema.iam.infrastructure.database.repositories.SpringDataPermissionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PostgresPermissionAdapter implements PermissionRepository {

    private final SpringDataPermissionRepository springDataPermissionRepository;

    public PostgresPermissionAdapter(SpringDataPermissionRepository springDataPermissionRepository) {
        this.springDataPermissionRepository = springDataPermissionRepository;
    }

    @Override
    public List<Permission> findAll() {
        return springDataPermissionRepository.findAll().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Permission> findByName(String name) {
        return springDataPermissionRepository.findByName(name).map(this::mapToDomain);
    }

    private Permission mapToDomain(PermissionJpaEntity entity) {
        return Permission.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }
}
