package com.example.cinema.application.ports.out;

public interface AuthGatewayPort {
    boolean verifyCredentials(String username, String password);
}
