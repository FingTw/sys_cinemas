package com.example.cinema.application.ports.out;

import com.example.cinema.domain.entities.Role;
import java.util.Optional;
import java.util.List;

public interface RoleRepositoryPort {
    Optional<Role> findByName(String name);
    List<Role> findAll();
}
