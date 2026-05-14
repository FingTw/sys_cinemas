package com.example.cinema.common.security.ports;

public interface AuthGatewayPort {
    boolean verifyCredentials(String username, String password);
}
