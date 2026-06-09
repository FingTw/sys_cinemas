package com.example.cinema.admin.infrastructure.database.adapters;

import com.example.cinema.admin.domain.entities.Permission;
import com.example.cinema.admin.domain.repositories.PermissionRepository;
import com.example.cinema.admin.infrastructure.database.entities.PermissionJpaEntity;
import com.example.cinema.admin.infrastructure.database.repositories.SpringDataPermissionRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PermissionRepositoryAdapter implements PermissionRepository {

    private final SpringDataPermissionRepository springDataPermissionRepository;

    public PermissionRepositoryAdapter(SpringDataPermissionRepository springDataPermissionRepository) {
        this.springDataPermissionRepository = springDataPermissionRepository;
    }

    @Override
    public List<Permission> findAll() {
        return springDataPermissionRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private Permission toDomain(PermissionJpaEntity entity) {
        return Permission.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }
}
