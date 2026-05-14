package com.example.cinema.iam.infrastructure.database.adapters;

import com.example.cinema.iam.domain.repositories.RoleRepository;
import com.example.cinema.iam.domain.entities.Role;
import com.example.cinema.iam.infrastructure.database.entities.RoleJpaEntity;
import com.example.cinema.iam.infrastructure.database.repositories.SpringDataRoleRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
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
