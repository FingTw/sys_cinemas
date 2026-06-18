package com.example.cinema.iam.infrastructure.database.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Entity
@Table(name = "sso_role_mappings", schema = "auth")
@Getter
@Setter
@NoArgsConstructor
public class SsoRoleMappingJpaEntity {

    @Id
    private UUID id;

    @Column(name = "sso_role_name", unique = true, nullable = false)
    private String ssoRoleName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_role_id", nullable = false)
    private RoleJpaEntity localRole;

    public SsoRoleMappingJpaEntity(UUID id, String ssoRoleName, RoleJpaEntity localRole) {
        this.id = id;
        this.ssoRoleName = ssoRoleName;
        this.localRole = localRole;
    }
}
