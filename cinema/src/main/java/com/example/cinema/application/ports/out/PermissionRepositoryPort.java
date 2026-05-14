package com.example.cinema.application.ports.out;

import com.example.cinema.domain.entities.Permission;
import java.util.List;
import java.util.Optional;

public interface PermissionRepositoryPort {
    List<Permission> findAll();
    Optional<Permission> findByName(String name);
}
