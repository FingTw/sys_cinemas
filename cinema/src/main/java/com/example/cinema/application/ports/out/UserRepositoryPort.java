package com.example.cinema.application.ports.out;

import com.example.cinema.domain.entities.User;
import java.util.Optional;

public interface UserRepositoryPort {
    Optional<User> findByUsername(String username);
    Optional<User> findById(String id);
    java.util.List<User> findAll();
    void save(User user);
    void deleteById(String id);
    boolean existsByUsername(String username);
    Long findTokenVersionById(String userId);
    Long incrementTokenVersion(String userId);
}
