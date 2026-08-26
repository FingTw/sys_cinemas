package com.example.cinema.auth.services;

import com.example.cinema.auth.dto.RegisterRequest;
// import RegisterUserService;
import com.example.cinema.auth.entities.User;
import com.example.cinema.auth.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.springframework.beans.factory.annotation.Value;
import jakarta.ws.rs.core.Response;
import java.util.Collections;

@Service
public class RegisterUserService {

    private final UserRepository userRepository;
    
    @Value("${keycloak.url:http://localhost:8080}")
    private String keycloakUrl;
    
    @Value("${keycloak.realm:sys-cinema}")
    private String realm;
    
    @Value("${keycloak.admin.client-id:admin-cli}")
    private String adminClientId;
    
    @Value("${keycloak.admin.username:admin}")
    private String adminUsername;
    
    @Value("${keycloak.admin.password:admin}")
    private String adminPassword;

    public RegisterUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

        @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // 1. Create user in Keycloak
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(keycloakUrl)
                .realm("master")
                .clientId(adminClientId)
                .username(adminUsername)
                .password(adminPassword)
                .build();

        UserRepresentation kcUser = new UserRepresentation();
        kcUser.setUsername(request.getUsername());
        kcUser.setEmail(request.getEmail());
        kcUser.setEnabled(true);
        kcUser.setEmailVerified(true);
        
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.getPassword());
        credential.setTemporary(false);
        kcUser.setCredentials(Collections.singletonList(credential));

        Response response = keycloak.realm(realm).users().create(kcUser);
        
        if (response.getStatus() != 201) {
            throw new RuntimeException("Failed to create user in Keycloak: " + response.getStatusInfo().getReasonPhrase());
        }

        String userId = org.keycloak.admin.client.CreatedResponseUtil.getCreatedId(response);

        // 2. Save user profile in local DB
        User localUser = User.builder()
                .id(userId)
                .username(request.getUsername())
                .email(request.getEmail())
                .ssoSubject(userId)
                .isBlocked(false)
                .build();

        userRepository.save(localUser);
    }
}
