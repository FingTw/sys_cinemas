package com.example.cinema.iam.infrastructure.database.repositories;

import com.example.cinema.iam.infrastructure.database.entities.PasswordPolicyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataPasswordPolicyRepository extends JpaRepository<PasswordPolicyJpaEntity, String> {
}
