package com.example.cinema.admin.application.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class SecurityUtils {

    public static String getStaffCinemaId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            boolean isStaff = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("ROLE_STAFF"));
                    
            if (isStaff && auth.getDetails() instanceof WebAuthenticationDetails) {
                return ((WebAuthenticationDetails) auth.getDetails()).getSessionId();
            }
        }
        return null; // Return null if not STAFF or no cinemaId
    }
}
