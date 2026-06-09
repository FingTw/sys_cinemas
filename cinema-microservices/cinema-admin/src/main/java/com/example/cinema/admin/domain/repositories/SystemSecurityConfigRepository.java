package com.example.cinema.admin.domain.repositories;

import com.example.cinema.admin.domain.entities.SystemSecurityConfig;
import java.util.Optional;

public interface SystemSecurityConfigRepository {
    SystemSecurityConfig save(SystemSecurityConfig config);
    Optional<SystemSecurityConfig> findById(String id);
}
