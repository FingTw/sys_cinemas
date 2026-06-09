package com.example.cinema.iam.domain.repositories;

import com.example.cinema.iam.domain.entities.SystemSecurityConfig;

import java.util.Optional;

public interface SystemSecurityConfigRepository {
    Optional<SystemSecurityConfig> findById(String id);
    void save(SystemSecurityConfig config);
}
