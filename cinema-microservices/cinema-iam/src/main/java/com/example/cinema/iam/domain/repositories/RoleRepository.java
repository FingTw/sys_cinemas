package com.example.cinema.iam.domain.repositories;

import com.example.cinema.iam.domain.entities.Role;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository {
    Optional<Role> findByName(String name);
    Optional<Role> findById(UUID id);
    java.util.List<Role> findAll();
    Role save(Role role);
    void deleteById(UUID id);
}
