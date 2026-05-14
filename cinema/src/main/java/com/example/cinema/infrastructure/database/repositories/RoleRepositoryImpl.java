package com.example.cinema.infrastructure.database.repositories;

import com.example.cinema.domain.entities.Role;
import com.example.cinema.domain.repositories.RoleRepository;
import com.example.cinema.infrastructure.database.entities.RoleJpaEntity;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class RoleRepositoryImpl implements RoleRepository {

    private final SpringDataRoleRepository springDataRoleRepository;

    public RoleRepositoryImpl(SpringDataRoleRepository springDataRoleRepository) {
        this.springDataRoleRepository = springDataRoleRepository;
    }

    @Override
    public Optional<Role> findByName(String name) {
        return springDataRoleRepository.findByName(name)
                .map(jpa -> Role.builder()
                        .id(jpa.getId())
                        .name(jpa.getName())
                        .permissions(jpa.getPermissions().stream()
                                .map(p -> com.example.cinema.domain.entities.Permission.builder()
                                        .id(p.getId())
                                        .name(p.getName())
                                        .description(p.getDescription())
                                        .build())
                                .collect(java.util.stream.Collectors.toSet()))
                        .build());
    }
}
