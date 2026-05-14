package com.example.cinema.infrastructure.database.adapters;

import com.example.cinema.application.ports.out.RoleRepositoryPort;
import com.example.cinema.domain.entities.Role;
import com.example.cinema.infrastructure.database.entities.RoleJpaEntity;
import com.example.cinema.infrastructure.database.repositories.SpringDataRoleRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PostgresRoleAdapter implements RoleRepositoryPort {

    private final SpringDataRoleRepository springDataRoleRepository;

    public PostgresRoleAdapter(SpringDataRoleRepository springDataRoleRepository) {
        this.springDataRoleRepository = springDataRoleRepository;
    }

    @Override
    public Optional<Role> findByName(String name) {
        return springDataRoleRepository.findByName(name).map(this::mapToDomain);
    }

    @Override
    public List<Role> findAll() {
        return springDataRoleRepository.findAll().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    private Role mapToDomain(RoleJpaEntity entity) {
        return Role.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }
}
