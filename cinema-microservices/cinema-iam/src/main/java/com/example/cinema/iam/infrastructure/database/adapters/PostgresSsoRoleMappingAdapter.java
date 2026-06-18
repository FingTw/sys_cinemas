package com.example.cinema.iam.infrastructure.database.adapters;

import com.example.cinema.iam.domain.entities.SsoRoleMapping;
import com.example.cinema.iam.domain.entities.Role;
import com.example.cinema.iam.domain.entities.Permission;
import com.example.cinema.iam.domain.repositories.SsoRoleMappingRepository;
import com.example.cinema.iam.infrastructure.database.entities.SsoRoleMappingJpaEntity;
import com.example.cinema.iam.infrastructure.database.entities.RoleJpaEntity;
import com.example.cinema.iam.infrastructure.database.repositories.SpringDataSsoRoleMappingRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PostgresSsoRoleMappingAdapter implements SsoRoleMappingRepository {

    private final SpringDataSsoRoleMappingRepository springDataRepository;

    public PostgresSsoRoleMappingAdapter(SpringDataSsoRoleMappingRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Optional<SsoRoleMapping> findBySsoRoleName(String ssoRoleName) {
        return springDataRepository.findBySsoRoleName(ssoRoleName).map(this::mapToDomain);
    }

    @Override
    public SsoRoleMapping save(SsoRoleMapping mapping) {
        SsoRoleMappingJpaEntity entity = mapToJpa(mapping);
        SsoRoleMappingJpaEntity saved = springDataRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public void deleteById(String id) {
        springDataRepository.deleteById(UUID.fromString(id));
    }

    @Override
    public List<SsoRoleMapping> findAll() {
        return springDataRepository.findAll().stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    private SsoRoleMapping mapToDomain(SsoRoleMappingJpaEntity entity) {
        if (entity == null) return null;
        
        Role role = Role.builder()
                .id(entity.getLocalRole().getId())
                .name(entity.getLocalRole().getName())
                .permissions(entity.getLocalRole().getPermissions().stream()
                        .map(p -> Permission.builder()
                                .id(p.getId())
                                .name(p.getName())
                                .description(p.getDescription())
                                .build())
                        .collect(Collectors.toSet()))
                .build();

        return SsoRoleMapping.builder()
                .id(entity.getId() != null ? entity.getId().toString() : null)
                .ssoRoleName(entity.getSsoRoleName())
                .localRole(role)
                .build();
    }

    private SsoRoleMappingJpaEntity mapToJpa(SsoRoleMapping domain) {
        if (domain == null) return null;
        SsoRoleMappingJpaEntity entity = new SsoRoleMappingJpaEntity();
        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }
        entity.setSsoRoleName(domain.getSsoRoleName());
        
        if (domain.getLocalRole() != null) {
            RoleJpaEntity roleJpa = new RoleJpaEntity();
            roleJpa.setId(domain.getLocalRole().getId());
            roleJpa.setName(domain.getLocalRole().getName());
            entity.setLocalRole(roleJpa);
        }
        
        return entity;
    }
}
