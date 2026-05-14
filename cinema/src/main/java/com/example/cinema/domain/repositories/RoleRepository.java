package com.example.cinema.domain.repositories;

import com.example.cinema.domain.entities.Role;
import java.util.Optional;

public interface RoleRepository {
    Optional<Role> findByName(String name);
}
