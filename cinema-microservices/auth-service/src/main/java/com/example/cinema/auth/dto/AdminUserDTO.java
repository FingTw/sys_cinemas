package com.example.cinema.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserDTO {
    private String id;
    private String username;
    private String email;
    private String roles;
    private String permissions;

    @JsonProperty("isBlocked")
    private boolean isBlocked;

    @JsonProperty("isOnline")
    private boolean isOnline;
}
