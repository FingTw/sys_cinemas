package com.example.cinema.admin.domain.repositories;

import com.example.cinema.admin.domain.entities.PasswordPolicy;
import java.util.Optional;

public interface PasswordPolicyRepository {
    PasswordPolicy save(PasswordPolicy policy);
    Optional<PasswordPolicy> findById(String id);
}
