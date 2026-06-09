package com.example.cinema.iam.domain.repositories;

import com.example.cinema.iam.domain.entities.PasswordPolicy;
import java.util.Optional;

public interface PasswordPolicyRepository {
    Optional<PasswordPolicy> findById(String id);
    void save(PasswordPolicy policy);
}
