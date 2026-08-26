package com.example.cinema.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component("cinemaSecurity")
public class CinemaSecurity {
    
    private static final Logger log = LoggerFactory.getLogger(CinemaSecurity.class);

    public boolean hasAccess(Authentication authentication, String targetCinemaId) {
        if (authentication == null || targetCinemaId == null || targetCinemaId.trim().isEmpty()) {
            return false;
        }

        // Global admin has access to all cinemas
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(SecurityRoles.ROLE_ADMIN));
        if (isAdmin) {
            return true;
        }

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String userCinemaId = jwt.getClaimAsString("cinema_id");
            if (userCinemaId == null) {
                log.warn("JWT token missing cinema_id claim for user: {}", authentication.getName());
                return false;
            }
            return targetCinemaId.equals(userCinemaId);
        }

        return false;
    }
}
