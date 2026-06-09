package com.example.cinema.iam.domain.repositories;

import java.util.Optional;

import com.example.cinema.iam.domain.entities.User;

// Chỉ là interface định nghĩa nghiệp vụ. KHÔNG extends JpaRepository.
public interface UserRepository {

    Optional<User> findByUsername(String username);

    void save(User user);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    java.util.List<User> findAll();

    Optional<User> findById(String id);
    void deleteById(String id);
    Long findTokenVersionById(String userId);
    Long incrementTokenVersion(String userId);

}
