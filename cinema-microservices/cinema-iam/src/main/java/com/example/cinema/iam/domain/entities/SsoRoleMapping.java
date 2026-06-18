package com.example.cinema.iam.domain.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class SsoRoleMapping {
    private String id;
    private String ssoRoleName;
    private Role localRole;

    @Builder
    public SsoRoleMapping(String id, String ssoRoleName, Role localRole) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.ssoRoleName = ssoRoleName;
        this.localRole = localRole;
    }
}
