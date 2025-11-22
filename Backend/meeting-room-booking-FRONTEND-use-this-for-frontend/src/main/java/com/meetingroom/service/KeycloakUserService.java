package com.meetingroom.service;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import jakarta.ws.rs.core.Response;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class KeycloakUserService {

    @Value("${keycloak.url}")
    private String keycloakUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client.id}")
    private String clientId;

    @Value("${keycloak.client.secret}")
    private String clientSecret;

    @Autowired
    private Keycloak keycloak;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Create a new user in Keycloak
     */
    public String createKeycloakUser(String email, String password, String firstName, String lastName) {
        try {
            // Create user representation
            UserRepresentation user = new UserRepresentation();
            user.setUsername(email);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEnabled(true);
            user.setEmailVerified(true);

            // Create password credential
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(false);
            user.setCredentials(Collections.singletonList(credential));

            // Create user in Keycloak
            Response response = keycloak.realm(realm)
                    .users()
                    .create(user);

            if (response.getStatus() == 201) {
                // Extract user ID from Location header
                String locationHeader = response.getHeaderString("Location");
                String userId = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);
                
                System.out.println("✓ User created in Keycloak with ID: " + userId);
                response.close();
                return userId;
            } else {
                String errorMessage = "Failed to create user in Keycloak. Status: " + response.getStatus();
                response.close();
                throw new RuntimeException(errorMessage);
            }
        } catch (Exception e) {
            System.err.println("Error creating Keycloak user: " + e.getMessage());
            throw new RuntimeException("Failed to create user in Keycloak: " + e.getMessage());
        }
    }

    /**
     * Check if user exists in Keycloak by email
     */
    public boolean userExistsInKeycloak(String email) {
        try {
            List<UserRepresentation> users = keycloak.realm(realm)
                    .users()
                    .search(email, true);
            return !users.isEmpty();
        } catch (Exception e) {
            System.err.println("Error checking if user exists: " + e.getMessage());
            return false;
        }
    }

    /**
     * Authenticate user and get tokens
     */
    public Map<String, Object> authenticateUser(String username, String password) {
        String tokenUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("username", username);
        body.add("password", password);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to authenticate with Keycloak: " + e.getMessage());
        }
    }

    /**
     * Refresh access token using refresh token
     */
    public Map<String, Object> refreshToken(String refreshToken) {
        String tokenUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to refresh token: " + e.getMessage());
        }
    }
}