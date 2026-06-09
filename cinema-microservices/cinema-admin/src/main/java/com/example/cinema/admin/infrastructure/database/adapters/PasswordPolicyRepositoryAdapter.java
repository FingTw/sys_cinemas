package com.example.cinema.admin.infrastructure.database.adapters;

import com.example.cinema.admin.domain.entities.PasswordPolicy;
import com.example.cinema.admin.domain.repositories.PasswordPolicyRepository;
import com.example.cinema.admin.infrastructure.database.entities.PasswordPolicyJpaEntity;
import com.example.cinema.admin.infrastructure.database.repositories.SpringDataPasswordPolicyRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class PasswordPolicyRepositoryAdapter implements PasswordPolicyRepository {

    private final SpringDataPasswordPolicyRepository springDataPasswordPolicyRepository;

    public PasswordPolicyRepositoryAdapter(SpringDataPasswordPolicyRepository springDataPasswordPolicyRepository) {
        this.springDataPasswordPolicyRepository = springDataPasswordPolicyRepository;
    }

    @Override
    public PasswordPolicy save(PasswordPolicy policy) {
        PasswordPolicyJpaEntity entity = toEntity(policy);
        PasswordPolicyJpaEntity saved = springDataPasswordPolicyRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<PasswordPolicy> findById(String id) {
        return springDataPasswordPolicyRepository.findById(id).map(this::toDomain);
    }

    private PasswordPolicy toDomain(PasswordPolicyJpaEntity entity) {
        return PasswordPolicy.builder()
                .id(entity.getId())
                .minLength(entity.getMinLength())
                .requireUppercase(entity.isRequireUppercase())
                .requireLowercase(entity.isRequireLowercase())
                .requireNumber(entity.isRequireNumber())
                .requireSpecialChar(entity.isRequireSpecialChar())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private PasswordPolicyJpaEntity toEntity(PasswordPolicy domain) {
        return PasswordPolicyJpaEntity.builder()
                .id(domain.getId())
                .minLength(domain.getMinLength())
                .requireUppercase(domain.isRequireUppercase())
                .requireLowercase(domain.isRequireLowercase())
                .requireNumber(domain.isRequireNumber())
                .requireSpecialChar(domain.isRequireSpecialChar())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
