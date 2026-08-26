package com.example.cinema.admin.dto;

import java.util.Set;
import java.util.UUID;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleDTO {
    private UUID id;
    private String name;
    private Set<String> permissions;
}
