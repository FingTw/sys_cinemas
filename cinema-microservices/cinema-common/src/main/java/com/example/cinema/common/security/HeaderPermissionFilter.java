package com.example.cinema.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Filter này dùng để nhận X-User-Roles và X-User-Permissions từ Gateway
 * và gán vào SecurityContextHolder cho Spring Security phân quyền (@PreAuthorize).
 */
@Component
public class HeaderPermissionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
            
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String rolesHeader = request.getHeader("X-User-Roles");
        String permissionsHeader = request.getHeader("X-User-Permissions");
        String userId = request.getHeader("X-User-Id");

        // Nếu request có đi qua Gateway và truyền Header (ít nhất là userId)
        if (rolesHeader != null || permissionsHeader != null || userId != null) {
            Collection<GrantedAuthority> newAuthorities = new ArrayList<>();

            if (rolesHeader != null && !rolesHeader.isEmpty()) {
                String[] roles = rolesHeader.split(",");
                for (String role : roles) {
                    if (!role.trim().isEmpty()) {
                        newAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role.trim().toUpperCase()));
                    }
                }
            }

            if (permissionsHeader != null && !permissionsHeader.isEmpty()) {
                String[] permissions = permissionsHeader.split(",");
                for (String perm : permissions) {
                    if (!perm.trim().isEmpty()) {
                        newAuthorities.add(new SimpleGrantedAuthority(perm.trim().toUpperCase()));
                    }
                }
            }

            // Lấy ID người dùng từ Header (nếu có do Gateway truyền xuống)
            if (userId == null || userId.isEmpty()) {
                // Thử lấy Authorization: Bearer để trích xuất sub (fallback)
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    userId = "authenticated-user"; // Hoặc parse JWT payload nếu cần
                } else {
                    userId = "anonymous";
                }
            }

            // Tạo Token mới chứa quyền nội bộ
            org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken newAuthToken = 
                new org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken(
                    userId, 
                    null, 
                    newAuthorities
            );

            SecurityContextHolder.getContext().setAuthentication(newAuthToken);
        }

        filterChain.doFilter(request, response);
    }
}
