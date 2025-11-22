package com.meetingroom.service;

import com.meetingroom.model.User;
import com.meetingroom.repository.UserRepository;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service to sync existing database users with Keycloak
 * Use this ONLY ONCE to migrate existing users
 */
@Service
public class UserSyncService {

    @Autowired
    private Keycloak keycloak;

    @Autowired
    private UserRepository userRepository;

    @Value("${keycloak.realm}")
    private String realm;

    /**
     * Syncs users from database to Keycloak by searching for existing Keycloak users
     * and updating the database with their Keycloak IDs
     */
    @Transactional
    public void syncExistingUsers() {
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        // Get all users from database that don't have a Keycloak ID
        List<User> usersWithoutKeycloakId = userRepository.findAll().stream()
                .filter(user -> user.getKeycloakId() == null || user.getKeycloakId().isEmpty())
                .toList();

        System.out.println("Found " + usersWithoutKeycloakId.size() + " users without Keycloak ID");

        for (User user : usersWithoutKeycloakId) {
            try {
                // Search for user in Keycloak by email
                List<UserRepresentation> keycloakUsers = usersResource.search(user.getEmail(), true);

                if (!keycloakUsers.isEmpty()) {
                    // User exists in Keycloak - update database with Keycloak ID
                    UserRepresentation keycloakUser = keycloakUsers.get(0);
                    user.setKeycloakId(keycloakUser.getId());
                    userRepository.save(user);
                    System.out.println("✓ Synced user: " + user.getEmail() + " with Keycloak ID: " + keycloakUser.getId());
                } else {
                    // User doesn't exist in Keycloak
                    System.out.println("✗ User not found in Keycloak: " + user.getEmail());
                    System.out.println("  You need to create this user in Keycloak manually or delete from database");
                }
            } catch (Exception e) {
                System.err.println("✗ Error syncing user " + user.getEmail() + ": " + e.getMessage());
            }
        }

        System.out.println("Sync completed!");
    }

    /**
     * Deletes users from database that don't have a Keycloak ID
     * WARNING: This will permanently delete users!
     */
    @Transactional
    public void deleteUsersWithoutKeycloakId() {
        List<User> usersToDelete = userRepository.findAll().stream()
                .filter(user -> user.getKeycloakId() == null || user.getKeycloakId().isEmpty())
                .toList();

        System.out.println("Deleting " + usersToDelete.size() + " users without Keycloak ID");

        for (User user : usersToDelete) {
            System.out.println("Deleting user: " + user.getEmail());
            userRepository.delete(user);
        }

        System.out.println("Deletion completed!");
    }
}
