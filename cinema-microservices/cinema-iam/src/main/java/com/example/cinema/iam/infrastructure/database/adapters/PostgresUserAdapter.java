package com.example.cinema.iam.infrastructure.database.adapters;

import com.example.cinema.iam.domain.repositories.UserRepository;
import com.example.cinema.iam.domain.entities.User;
import com.example.cinema.iam.domain.entities.Role;
import com.example.cinema.iam.domain.entities.Permission;
import com.example.cinema.iam.infrastructure.database.entities.UserJpaEntity;
import com.example.cinema.iam.infrastructure.database.entities.RoleJpaEntity;
import com.example.cinema.iam.infrastructure.database.entities.PermissionJpaEntity;
import com.example.cinema.iam.infrastructure.database.repositories.SpringDataUserRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PostgresUserAdapter implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;

    public PostgresUserAdapter(SpringDataUserRepository springDataUserRepository) {
        this.springDataUserRepository = springDataUserRepository;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return springDataUserRepository.findByUsername(username).map(this::mapToDomain);
    }

    @Override
    public Optional<User> findById(String id) {
        return springDataUserRepository.findById(id).map(this::mapToDomain);
    }

    @Override
    public java.util.List<User> findAll() {
        return springDataUserRepository.findAll().stream()
            .map(this::mapToDomain)
            .collect(Collectors.toList());
    }

    @Override
    public void save(User user) {
        UserJpaEntity entity = mapToJpa(user);
        springDataUserRepository.save(entity);
    }

    @Override
    public void deleteById(String id) {
        springDataUserRepository.deleteById(id);
    }

    @Override
    public boolean existsByUsername(String username) {
        return springDataUserRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataUserRepository.existsByEmail(email);
    }

    private User mapToDomain(UserJpaEntity entity) {
        return User.builder()
            .id(entity.getId())
            .username(entity.getUsername())
            .email(entity.getEmail())
            .password(entity.getPassword())
            .roles(entity.getRoles().stream()
                .map(r -> Role.builder()
                    .id(r.getId())
                    .name(r.getName())
                    .permissions(r.getPermissions().stream()
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
            .isBlocked(entity.isBlocked())
            .activeToken(entity.getActiveToken())
            .tokenVersion(entity.getTokenVersion())
            .build();
    }

    private UserJpaEntity mapToJpa(User user) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.getId());
        entity.setUsername(user.getUsername());
        entity.setEmail(user.getEmail());
        entity.setPassword(user.getPassword());
        entity.setRoles(user.getRoles().stream()
            .map(r -> {
                RoleJpaEntity role = new RoleJpaEntity();
                role.setId(r.getId());
                role.setName(r.getName());
                role.setPermissions(r.getPermissions().stream()
                    .map(p -> {
                        PermissionJpaEntity perm = new PermissionJpaEntity();
                        perm.setId(p.getId());
                        perm.setName(p.getName());
                        perm.setDescription(p.getDescription());
                        return perm;
                    })
                    .collect(Collectors.toSet()));
                return role;
            })
            .collect(Collectors.toSet()));
        entity.setPermissions(user.getPermissions().stream()
            .map(p -> {
                PermissionJpaEntity perm = new PermissionJpaEntity();
                perm.setId(p.getId());
                perm.setName(p.getName());
                perm.setDescription(p.getDescription());
                return perm;
            })
            .collect(Collectors.toSet()));
        entity.setBlocked(user.isBlocked());
        entity.setActiveToken(user.getActiveToken());
        entity.setTokenVersion(user.getTokenVersion());
        return entity;
    }

    @Override
    public Long findTokenVersionById(String userId) {
        return springDataUserRepository.findTokenVersionById(userId);
    }

    @Override
    public Long incrementTokenVersion(String userId) {
        UserJpaEntity user = springDataUserRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        Long currentVersion = user.getTokenVersion() != null ? user.getTokenVersion() : 0L;
        user.setTokenVersion(currentVersion + 1);
        springDataUserRepository.save(user);
        return user.getTokenVersion();
    }
}
