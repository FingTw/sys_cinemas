package com.example.cinema.spi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.mindrot.jbcrypt.BCrypt;

public class CinemaUserStorageProvider implements UserStorageProvider, UserLookupProvider, CredentialInputValidator {

    private final KeycloakSession session;
    private final ComponentModel model;

    public CinemaUserStorageProvider(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.model = model;
    }

    private Connection getConnection() throws Exception {
        // Dùng tên biến riêng để tránh conflict với KC_DB_URL của Keycloak internal
        String url = getenvOrDefault("CINEMA_DB_URL", "jdbc:postgresql://cinema-db:5432/cinema_db");
        String username = getenvOrDefault("CINEMA_DB_USERNAME", "postgres");
        String password = getenvOrDefault("CINEMA_DB_PASSWORD", "postgres123");

        System.err.println("=== SPI: Connecting to DB: " + url);
        Connection conn = DriverManager.getConnection(url, username, password);
        System.err.println("=== SPI: DB connection SUCCESS");
        return conn;
    }

    private String getenvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        System.err.println("=== SPI: getUserByUsername called for: [" + username + "]");
        try (Connection conn = getConnection();
                PreparedStatement st = conn.prepareStatement("SELECT id FROM auth.users WHERE username = ?")) {
            st.setString(1, username);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    System.err.println("=== SPI: User FOUND in DB: [" + username + "]");
                    return new CinemaUserAdapter(session, realm, model, username);
                } else {
                    System.err.println("=== SPI: User NOT FOUND in DB: [" + username + "]");
                }
            }
        } catch (Exception e) {
            System.err.println("=== SPI ERROR in getUserByUsername: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        System.err.println("=== SPI: isValid called for: [" + user.getUsername() + "]");

        if (!supportsCredentialType(input.getType())) {
            System.err.println("=== SPI: Unsupported credential type: " + input.getType());
            return false;
        }

        String rawPassword = input.getChallengeResponse();
        System.err.println(
                "=== SPI: Raw password received (length): " + (rawPassword != null ? rawPassword.length() : "null"));

        String hashedPassword = null;
        try (Connection conn = getConnection();
                PreparedStatement st = conn.prepareStatement("SELECT password FROM auth.users WHERE username = ?")) {
            st.setString(1, user.getUsername());
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    hashedPassword = rs.getString("password");
                    System.err.println("=== SPI: Hash found in DB: " + hashedPassword.substring(0, 10) + "...");
                } else {
                    System.err.println("=== SPI: No hash found for user: " + user.getUsername());
                }
            }
        } catch (Exception e) {
            System.err.println("=== SPI ERROR in isValid: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        if (hashedPassword == null) {
            return false;
        }

        // BCrypt check: password + username (khớp với logic register của App)
        String toCheck = rawPassword + user.getUsername();
        boolean result = BCrypt.checkpw(toCheck, hashedPassword);
        System.err.println("=== SPI: BCrypt check result: " + result);
        return result;
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return supportsCredentialType(credentialType);
    }

    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        return null;
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        return null;
    }

    @Override
    public void close() {
    }
}