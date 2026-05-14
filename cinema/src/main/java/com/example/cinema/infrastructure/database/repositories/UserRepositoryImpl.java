package com.example.cinema.infrastructure.database.repositories;

import com.example.cinema.domain.entities.Role;
import com.example.cinema.domain.entities.User;
import com.example.cinema.domain.entities.Permission;
import com.example.cinema.domain.repositories.UserRepository;
import com.example.cinema.infrastructure.database.entities.RoleJpaEntity;
import com.example.cinema.infrastructure.database.entities.UserJpaEntity;
import com.example.cinema.infrastructure.database.entities.PermissionJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class UserRepositoryImpl implements UserRepository {

    private final SpringDataUserRepository jpaRepository;

    public UserRepositoryImpl(SpringDataUserRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        Optional<UserJpaEntity> entityOptional = jpaRepository.findByUsername(username);
        return entityOptional.map(this::mapToDomain);
    }

    @Override
    public void save(User user) {
        UserJpaEntity entity = mapToJpa(user);
        jpaRepository.save(entity);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public java.util.List<User> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<User> findById(String id) {
        return jpaRepository.findById(id).map(this::mapToDomain);
    }

    private User mapToDomain(UserJpaEntity entity) {
        return User.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .roles(entity.getRoles().stream()
                        .map(roleEntity -> Role.builder()
                                .id(roleEntity.getId())
                                .name(roleEntity.getName())
                                .permissions(roleEntity.getPermissions().stream()
                                        .map(p -> Permission.builder()
                                                .id(p.getId())
                                                .name(p.getName())
                                                .description(p.getDescription())
                                                .build())
                                        .collect(Collectors.toSet()))
                                .build())
                        .collect(Collectors.toSet()))
                .permissions(entity.getPermissions().stream()
                        .map(p -> Permission.builder()
                                .id(p.getId())
                                .name(p.getName())
                                .description(p.getDescription())
                                .build())
                        .collect(Collectors.toSet()))
                .activeToken(entity.getActiveToken())
                .isBlocked(entity.isBlocked())
                .tokenVersion(entity.getTokenVersion())
                .build();
    }

    private UserJpaEntity mapToJpa(User user) {
        return UserJpaEntity.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRoles().stream()
                        .map(role -> {
                            RoleJpaEntity roleEntity = new RoleJpaEntity();
                            roleEntity.setId(role.getId());
                            roleEntity.setName(role.getName());
                            roleEntity.setPermissions(role.getPermissions().stream()
                                    .map(p -> new PermissionJpaEntity(p.getId(), p.getName(), p.getDescription()))
                                    .collect(Collectors.toSet()));
                            return roleEntity;
                        })
                        .collect(Collectors.toSet()))
                .permissions(user.getPermissions().stream()
                        .map(p -> new PermissionJpaEntity(p.getId(), p.getName(), p.getDescription()))
                        .collect(Collectors.toSet()))
                .activeToken(user.getActiveToken())
                .isBlocked(user.isBlocked())
                .tokenVersion(user.getTokenVersion())
                .build();
    }
}
