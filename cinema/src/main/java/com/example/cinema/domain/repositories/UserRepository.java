package com.example.cinema.domain.repositories;

import java.util.Optional;

import com.example.cinema.domain.entities.User;

// Chỉ là interface định nghĩa nghiệp vụ. KHÔNG extends JpaRepository.
public interface UserRepository {

    Optional<User> findByUsername(String username);

    void save(User user);

    boolean existsByUsername(String username);

    java.util.List<User> findAll();

    Optional<User> findById(String id);
}
