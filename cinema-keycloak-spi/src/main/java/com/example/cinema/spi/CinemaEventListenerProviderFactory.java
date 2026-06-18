package com.example.cinema.spi;

import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * CinemaEventListenerProviderFactory — Dang ky CinemaEventListenerProvider voi Keycloak.
 *
 * <p>De kich hoat, can vao Keycloak Admin Console:
 * Realm Settings → Events → Event Listeners → Them "cinema-event-listener"
 */
public class CinemaEventListenerProviderFactory implements EventListenerProviderFactory {

    public static final String PROVIDER_ID = "cinema-event-listener";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        return new CinemaEventListenerProvider(session);
    }

    @Override
    public void init(org.keycloak.Config.Scope config) {
        // Khong can cau hinh them
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // Khong can xu ly sau init
    }

    @Override
    public void close() {
        // Khong can dong tai nguyen
    }
}
