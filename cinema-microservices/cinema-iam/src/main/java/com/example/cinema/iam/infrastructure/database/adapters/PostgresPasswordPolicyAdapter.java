package com.example.cinema.iam.infrastructure.database.adapters;

import com.example.cinema.iam.domain.entities.PasswordPolicy;
import com.example.cinema.iam.domain.repositories.PasswordPolicyRepository;
import com.example.cinema.iam.infrastructure.database.entities.PasswordPolicyJpaEntity;
import com.example.cinema.iam.infrastructure.database.repositories.SpringDataPasswordPolicyRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PostgresPasswordPolicyAdapter implements PasswordPolicyRepository {

    private final SpringDataPasswordPolicyRepository repository;

    public PostgresPasswordPolicyAdapter(SpringDataPasswordPolicyRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<PasswordPolicy> findById(String id) {
        return repository.findById(id).map(this::mapToDomain);
    }

    @Override
    public void save(PasswordPolicy policy) {
        repository.save(mapToJpa(policy));
    }

    private PasswordPolicy mapToDomain(PasswordPolicyJpaEntity entity) {
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

    private PasswordPolicyJpaEntity mapToJpa(PasswordPolicy policy) {
        return PasswordPolicyJpaEntity.builder()
                .id(policy.getId())
                .minLength(policy.getMinLength())
                .requireUppercase(policy.isRequireUppercase())
                .requireLowercase(policy.isRequireLowercase())
                .requireNumber(policy.isRequireNumber())
                .requireSpecialChar(policy.isRequireSpecialChar())
                .updatedAt(policy.getUpdatedAt())
                .build();
    }
}
