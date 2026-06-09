package com.example.cinema.iam.infrastructure.database.adapters;

import com.example.cinema.iam.domain.repositories.RoleRepository;
import com.example.cinema.iam.domain.entities.Role;
import com.example.cinema.iam.domain.entities.Permission;
import com.example.cinema.iam.infrastructure.database.entities.RoleJpaEntity;
import com.example.cinema.iam.infrastructure.database.entities.PermissionJpaEntity;
import com.example.cinema.iam.infrastructure.database.repositories.SpringDataRoleRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PostgresRoleAdapter implements RoleRepository {

    private final SpringDataRoleRepository springDataRoleRepository;

    public PostgresRoleAdapter(SpringDataRoleRepository springDataRoleRepository) {
        this.springDataRoleRepository = springDataRoleRepository;
    }

    @Override
    public Optional<Role> findByName(String name) {
        return springDataRoleRepository.findByName(name).map(this::mapToDomain);
    }

    @Override
    public Optional<Role> findById(UUID id) {
        return springDataRoleRepository.findById(id).map(this::mapToDomain);
    }

    @Override
    public List<Role> findAll() {
        return springDataRoleRepository.findAll().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Role save(Role role) {
        RoleJpaEntity entity = mapToJpa(role);
        RoleJpaEntity saved = springDataRoleRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        springDataRoleRepository.deleteById(id);
    }

    private Role mapToDomain(RoleJpaEntity entity) {
        return Role.builder()
                .id(entity.getId())
                .name(entity.getName())
                .permissions(entity.getPermissions().stream()
                        .map(p -> Permission.builder()
                                .id(p.getId())
                                .name(p.getName())
                                .description(p.getDescription())
                                .build())
                        .collect(Collectors.toSet()))
                .build();
    }

    private RoleJpaEntity mapToJpa(Role role) {
        RoleJpaEntity entity = new RoleJpaEntity();
        entity.setId(role.getId());
        entity.setName(role.getName());
        entity.setPermissions(role.getPermissions().stream()
                .map(p -> {
                    PermissionJpaEntity perm = new PermissionJpaEntity();
                    perm.setId(p.getId());
                    perm.setName(p.getName());
                    perm.setDescription(p.getDescription());
                    return perm;
                })
                .collect(Collectors.toSet()));
        return entity;
    }
}
