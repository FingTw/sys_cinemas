package com.example.cinema.admin.infrastructure.database.adapters;

import com.example.cinema.admin.domain.entities.Permission;
import com.example.cinema.admin.domain.entities.Role;
import com.example.cinema.admin.domain.entities.User;
import com.example.cinema.admin.domain.repositories.UserRepository;
import com.example.cinema.admin.infrastructure.database.entities.PermissionJpaEntity;
import com.example.cinema.admin.infrastructure.database.entities.RoleJpaEntity;
import com.example.cinema.admin.infrastructure.database.entities.UserJpaEntity;
import com.example.cinema.admin.infrastructure.database.repositories.SpringDataUserRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;

    public UserRepositoryAdapter(SpringDataUserRepository springDataUserRepository) {
        this.springDataUserRepository = springDataUserRepository;
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = toEntity(user);
        UserJpaEntity saved = springDataUserRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<User> findById(String id) {
        return springDataUserRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<User> findAll() {
        return springDataUserRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void updateWorkplace(String userId, String cinemaId) {
        springDataUserRepository.updateWorkplace(userId, cinemaId);
    }

    private User toDomain(UserJpaEntity entity) {
        return User.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .password(entity.getPassword())
                .email(entity.getEmail())
                .roles(entity.getRoles().stream().map(this::toDomainRole).collect(Collectors.toSet()))
                .permissions(entity.getPermissions().stream().map(this::toDomainPermission).collect(Collectors.toSet()))
                .isBlocked(entity.isBlocked())
                .cinemaId(entity.getCinemaId())
                .build();
    }

    private UserJpaEntity toEntity(User domain) {
        return UserJpaEntity.builder()
                .id(domain.getId())
                .username(domain.getUsername())
                .password(domain.getPassword())
                .email(domain.getEmail())
                .roles(domain.getRoles().stream().map(this::toEntityRole).collect(Collectors.toSet()))
                .permissions(domain.getPermissions().stream().map(this::toEntityPermission).collect(Collectors.toSet()))
                .isBlocked(domain.isBlocked())
                .cinemaId(domain.getCinemaId())
                .build();
    }

    private Role toDomainRole(RoleJpaEntity entity) {
        return Role.builder()
                .id(entity.getId())
                .name(entity.getName())
                .permissions(entity.getPermissions().stream().map(this::toDomainPermission).collect(Collectors.toSet()))
                .build();
    }

    private RoleJpaEntity toEntityRole(Role domain) {
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
