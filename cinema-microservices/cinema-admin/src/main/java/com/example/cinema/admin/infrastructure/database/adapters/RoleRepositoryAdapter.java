package com.example.cinema.admin.infrastructure.database.adapters;

import com.example.cinema.admin.domain.entities.Permission;
import com.example.cinema.admin.domain.entities.Role;
import com.example.cinema.admin.domain.repositories.RoleRepository;
import com.example.cinema.admin.infrastructure.database.entities.PermissionJpaEntity;
import com.example.cinema.admin.infrastructure.database.entities.RoleJpaEntity;
import com.example.cinema.admin.infrastructure.database.repositories.SpringDataRoleRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RoleRepositoryAdapter implements RoleRepository {

    private final SpringDataRoleRepository springDataRoleRepository;
    private final com.example.cinema.admin.infrastructure.database.repositories.SpringDataPermissionRepository permissionRepository;

    public RoleRepositoryAdapter(SpringDataRoleRepository springDataRoleRepository,
                                 com.example.cinema.admin.infrastructure.database.repositories.SpringDataPermissionRepository permissionRepository) {
        this.springDataRoleRepository = springDataRoleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public Role save(Role role) {
        RoleJpaEntity entity;
        if (role.getId() != null && springDataRoleRepository.existsById(role.getId())) {
            entity = springDataRoleRepository.findById(role.getId()).orElseThrow();
            entity.setName(role.getName());
            
            // Lấy các Permission đã được quản lý (managed entities) từ database
            List<UUID> permIds = role.getPermissions().stream()
                    .map(Permission::getId).collect(Collectors.toList());
            entity.setPermissions(new java.util.HashSet<>(permissionRepository.findAllById(permIds)));
        } else {
            entity = toEntity(role);
            if (!role.getPermissions().isEmpty()) {
                List<UUID> permIds = role.getPermissions().stream()
                        .map(Permission::getId).collect(Collectors.toList());
                entity.setPermissions(new java.util.HashSet<>(permissionRepository.findAllById(permIds)));
            }
        }
        RoleJpaEntity saved = springDataRoleRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Role> findById(UUID id) {
        return springDataRoleRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Role> findByName(String name) {
        return springDataRoleRepository.findByName(name).map(this::toDomain);
    }

    @Override
    public List<Role> findAll() {
        return springDataRoleRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        springDataRoleRepository.deleteById(id);
    }

    private Role toDomain(RoleJpaEntity entity) {
        return Role.builder()
                .id(entity.getId())
                .name(entity.getName())
                .permissions(entity.getPermissions().stream().map(this::toDomainPermission).collect(Collectors.toSet()))
                .build();
    }

    private RoleJpaEntity toEntity(Role domain) {
        RoleJpaEntity entity = new RoleJpaEntity(domain.getId(), domain.getName());
        entity.setPermissions(domain.getPermissions().stream().map(this::toEntityPermission).collect(Collectors.toSet()));
        return entity;
    }

    private Permission toDomainPermission(PermissionJpaEntity entity) {
        return Permission.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }

    private PermissionJpaEntity toEntityPermission(Permission domain) {
        return new PermissionJpaEntity(domain.getId(), domain.getName(), domain.getDescription());
    }
}
