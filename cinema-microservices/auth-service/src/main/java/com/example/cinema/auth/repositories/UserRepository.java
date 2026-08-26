package com.example.cinema.auth.repositories;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.cinema.auth.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);

    Optional<User> findBySsoSubject(String ssoSubject);

    Optional<User> findById(String id);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);

    @Query(value = "SELECT r.name FROM auth.roles r JOIN auth.user_roles ur ON r.id = ur.role_id WHERE ur.user_id = :userId", nativeQuery = true)
    List<String> findRolesByUserId(@Param("userId") String userId);

    @Query(value = "SELECT DISTINCT p.name FROM auth.permissions p " +
                   "LEFT JOIN auth.role_permissions rp ON p.id = rp.permission_id " +
                   "LEFT JOIN auth.user_roles ur ON rp.role_id = ur.role_id " +
                   "LEFT JOIN auth.user_permissions up ON p.id = up.permission_id " +
                   "WHERE ur.user_id = :userId OR up.user_id = :userId", nativeQuery = true)
    List<String> findPermissionsByUserId(@Param("userId") String userId);
}
