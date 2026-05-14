package com.example.cinema.spi;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProviderFactory;

public class CinemaUserStorageProviderFactory implements UserStorageProviderFactory<CinemaUserStorageProvider> {

    @Override
    public String getId() {
        // Tên này sẽ hiển thị trong màn hình Admin của Keycloak
        return "cinema-postgres-spi";
    }

    @Override
    public CinemaUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        return new CinemaUserStorageProvider(session, model);
    }
}
