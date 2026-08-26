package com.example.cinema.admin.repositories;

import com.example.cinema.admin.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"permissions"})
    Optional<Role> findByName(String name);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"permissions"})
    Optional<Role> findById(UUID id);
}
