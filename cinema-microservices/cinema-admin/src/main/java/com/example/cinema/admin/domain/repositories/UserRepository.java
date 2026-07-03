package com.example.cinema.admin.domain.repositories;

import com.example.cinema.admin.domain.entities.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(String id);
    List<User> findAll();
    void updateWorkplace(String userId, String cinemaId);
}
