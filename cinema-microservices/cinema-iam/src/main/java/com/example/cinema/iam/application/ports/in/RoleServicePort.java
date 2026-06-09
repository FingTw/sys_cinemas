package com.example.cinema.iam.application.ports.in;

import com.example.cinema.iam.application.dto.RoleDTO;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface RoleServicePort {
    List<RoleDTO> getAllRoles();
}
