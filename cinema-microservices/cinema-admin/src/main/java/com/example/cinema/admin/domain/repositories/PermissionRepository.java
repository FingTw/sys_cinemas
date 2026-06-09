package com.example.cinema.admin.domain.repositories;

import com.example.cinema.admin.domain.entities.Permission;
import java.util.List;

public interface PermissionRepository {
    List<Permission> findAll();
}
