package com.example.cinema.admin.repositories;

import com.example.cinema.admin.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<User> findByUsername(String username);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<User> findById(String id);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE User u SET u.cinemaId = :cinemaId WHERE u.id = :id")
    void updateWorkplace(@org.springframework.data.repository.query.Param("id") String id, @org.springframework.data.repository.query.Param("cinemaId") String cinemaId);
}
