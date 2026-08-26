package com.example.cinema.admin.repositories;

import com.example.cinema.admin.entities.SystemSecurityConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemSecurityConfigRepository extends JpaRepository<SystemSecurityConfig, String> {
}
