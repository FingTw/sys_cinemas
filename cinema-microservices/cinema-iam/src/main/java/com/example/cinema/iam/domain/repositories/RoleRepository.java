package com.example.cinema.iam.domain.repositories;

import com.example.cinema.iam.domain.entities.Role;
import java.util.Optional;

public interface RoleRepository {
    Optional<Role> findByName(String name);
    java.util.List<Role> findAll();
}
