package com.example.cinema.iam.application.usecases;

import com.example.cinema.iam.application.dto.RoleDTO;
import com.example.cinema.iam.application.ports.in.RoleServicePort;
import com.example.cinema.iam.domain.entities.Role;
import com.example.cinema.iam.domain.entities.Permission;
import com.example.cinema.iam.domain.repositories.RoleRepository;
import com.example.cinema.iam.domain.repositories.PermissionRepository;
import com.example.cinema.iam.exception.IamException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleServicePort {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleServiceImpl(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }


    private RoleDTO mapToDTO(Role role) {
        Set<String> permissionNames = role.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());
        return new RoleDTO(role.getId(), role.getName(), permissionNames);
    }
}
