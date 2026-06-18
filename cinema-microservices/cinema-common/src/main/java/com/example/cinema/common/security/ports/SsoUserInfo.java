package com.example.cinema.common.security.ports;

import java.util.Set;

/**
 * SsoUserInfo - Thong tin nguoi dung tra ve tu Keycloak sau khi exchange authorization_code.
 * Su dung trong luong SSO (OIDC Authorization Code Flow).
 *
 * @param username Ten dang nhap (preferred_username claim trong Keycloak token)
 * @param email    Email cua nguoi dung
 * @param sub      Subject ID cua Keycloak (unique identifier trong Keycloak realm)
 * @param roles    Danh sach cac roles cua user tu Keycloak
 */
public record SsoUserInfo(String username, String email, String sub, Set<String> roles) {
}
