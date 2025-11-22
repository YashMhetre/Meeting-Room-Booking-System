


package com.meetingroom.service;

import com.meetingroom.dto.RegisterRequest;
import com.meetingroom.dto.LoginRequest;
import com.meetingroom.dto.TokenResponse;
import com.meetingroom.model.User;
import com.meetingroom.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;

@Service
public class UserRegistrationService {

    @Autowired
    private KeycloakUserService keycloakUserService;

    @Autowired
    private UserRepository userRepository;

    @Value("${keycloak.url}")
    private String keycloakUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client.id}")
    private String clientId;

    @Value("${keycloak.client.secret}")
    private String clientSecret;

    public TokenResponse registerUser(RegisterRequest request) {
        System.out.println("=== STARTING REGISTRATION ===");
        System.out.println("Email: " + request.getEmail());
        System.out.println("Name: " + request.getName());
        
        // 1. Check if user exists in our database
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User with email " + request.getEmail() + " already exists");
        }

        // 2. Extract first and last name
        String[] nameParts = request.getName().split(" ", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        // 3. Create user in Keycloak FIRST
        String keycloakUserId;
        try {
            System.out.println("Creating user in Keycloak...");
            keycloakUserId = keycloakUserService.createKeycloakUser(
                    request.getEmail(),
                    request.getPassword(),
                    firstName,
                    lastName
            );
            System.out.println("✓ User created in Keycloak with ID: " + keycloakUserId);
        } catch (Exception e) {
            System.err.println("✗ Failed to create user in Keycloak: " + e.getMessage());
            throw new RuntimeException("Failed to create user in Keycloak: " + e.getMessage());
        }

        // 4. Save user in database (separate transaction that won't rollback)
        User savedUser;
        try {
            System.out.println("Saving user to database...");
            savedUser = saveUserToDatabase(request.getEmail(), request.getName(), keycloakUserId);
            System.out.println("✓ User saved to database with ID: " + savedUser.getId());
        } catch (Exception e) {
            System.err.println("✗ Failed to save user to database: " + e.getMessage());
            throw new RuntimeException("User created in Keycloak but failed to save to database");
        }

        // 5. Try to get token (with retry logic)
        String token = null;
        int maxRetries = 3;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                System.out.println("Attempting to get token (attempt " + attempt + "/" + maxRetries + ")...");
                token = getTokenFromKeycloak(request.getEmail(), request.getPassword());
                System.out.println("✓ Token obtained successfully");
                break;
            } catch (Exception e) {
                System.err.println("✗ Token attempt " + attempt + " failed: " + e.getMessage());
                
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(1500); // Wait 1.5 seconds before retry
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    System.err.println("✗ All token attempts failed");
                    // User is registered but can't get token - return message
                    throw new RuntimeException(
                        "User registered successfully but unable to generate token. " +
                        "Please login using /api/auth/login"
                    );
                }
            }
        }

        System.out.println("=== REGISTRATION COMPLETED SUCCESSFULLY ===");
        return new TokenResponse(token);
    }

    /**
     * Save user to database in a separate transaction that won't rollback
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public User saveUserToDatabase(String email, String name, String keycloakUserId) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setKeycloakId(keycloakUserId);
        user.setRole(User.Role.USER);
        
        User savedUser = userRepository.save(user);
        userRepository.flush(); // Force immediate write to database
        
        System.out.println("User persisted to database: " + savedUser.getId());
        return savedUser;
    }

    public TokenResponse loginUser(LoginRequest request) {
        System.out.println("=== STARTING LOGIN ===");
        System.out.println("Email: " + request.getEmail());
        
        // 1. Verify user exists in our database
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    System.err.println("✗ User not found in database");
                    return new RuntimeException("Invalid email or password");
                });
        
        System.out.println("✓ User found in database with ID: " + user.getId());

        // 2. Get token from Keycloak (Keycloak validates password)
        try {
            String token = getTokenFromKeycloak(request.getEmail(), request.getPassword());
            System.out.println("✓ Login successful");
            System.out.println("=== LOGIN COMPLETED ===");
            return new TokenResponse(token);
        } catch (Exception e) {
            System.err.println("✗ Token generation failed: " + e.getMessage());
            throw new RuntimeException("Invalid email or password");
        }
    }

    private String getTokenFromKeycloak(String email, String password) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            
            String tokenUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";
            
            System.out.println("Token URL: " + tokenUrl);
            System.out.println("Client ID: " + clientId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "password");
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("username", email);
            body.add("password", password);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String accessToken = (String) response.getBody().get("access_token");
                if (accessToken != null) {
                    return accessToken;
                }
                throw new RuntimeException("Access token not found in response");
            } else {
                throw new RuntimeException("Failed to obtain token. Status: " + response.getStatusCode());
            }

        } catch (HttpClientErrorException e) {
            System.err.println("Keycloak token error: " + e.getStatusCode());
            System.err.println("Response: " + e.getResponseBodyAsString());
            throw new RuntimeException("Invalid credentials or Keycloak configuration error");
        } catch (Exception e) {
            System.err.println("Unexpected error getting token: " + e.getMessage());
            throw new RuntimeException("Failed to obtain token: " + e.getMessage());
        }
    }
}