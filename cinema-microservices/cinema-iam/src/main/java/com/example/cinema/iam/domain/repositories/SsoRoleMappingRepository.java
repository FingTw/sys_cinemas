package com.example.cinema.iam.domain.repositories;

import com.example.cinema.iam.domain.entities.SsoRoleMapping;
import java.util.Optional;
import java.util.List;

public interface SsoRoleMappingRepository {
    Optional<SsoRoleMapping> findBySsoRoleName(String ssoRoleName);
    SsoRoleMapping save(SsoRoleMapping mapping);
    void deleteById(String id);
    List<SsoRoleMapping> findAll();
}
