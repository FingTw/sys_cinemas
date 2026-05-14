package com.example.cinema.iam.domain.repositories;

import com.example.cinema.iam.domain.entities.Permission;
import java.util.List;
import java.util.Optional;

public interface PermissionRepository {
    List<Permission> findAll();
    Optional<Permission> findByName(String name);
}
