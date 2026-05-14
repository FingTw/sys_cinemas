package com.example.cinema.spi;

import org.keycloak.component.ComponentModel;
import org.keycloak.credential.UserCredentialManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SubjectCredentialManager;
import org.keycloak.storage.adapter.AbstractUserAdapter;

public class CinemaUserAdapter extends AbstractUserAdapter {

    private final String username;

    public CinemaUserAdapter(KeycloakSession session, RealmModel realm, ComponentModel model, String username) {
        super(session, realm, model);
        this.username = username;
    }

    @Override
    public String getUsername() {
        return username;
    }

    // Bổ sung hàm này để tương thích với Keycloak 26+
    @Override
    public SubjectCredentialManager credentialManager() {
        return new UserCredentialManager(session, realm, this);
    }
}
