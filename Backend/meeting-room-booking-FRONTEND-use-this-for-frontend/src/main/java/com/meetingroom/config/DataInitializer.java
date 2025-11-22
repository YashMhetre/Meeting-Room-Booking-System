package com.meetingroom.config;

import com.meetingroom.model.Room;
import com.meetingroom.model.User;
import com.meetingroom.repository.RoomRepository;
import com.meetingroom.repository.UserRepository;
import com.meetingroom.service.KeycloakUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KeycloakUserService keycloakUserService;

    @Override
    public void run(String... args) throws Exception {
        // Initialize rooms if they don't exist
        if (roomRepository.count() == 0) {
            Room room1 = new Room();
            room1.setName("Lagos Meeting Blue");
            room1.setCapacity(10);
            room1.setAvailable(true);
            roomRepository.save(room1);

            Room room2 = new Room();
            room2.setName("Mumbai Conference Red");
            room2.setCapacity(15);
            room2.setAvailable(true);
            roomRepository.save(room2);

            Room room3 = new Room();
            room3.setName("Berlin Tech Green");
            room3.setCapacity(8);
            room3.setAvailable(true);
            roomRepository.save(room3);

            Room room4 = new Room();
            room4.setName("Tokyo Innovation Yellow");
            room4.setCapacity(12);
            room4.setAvailable(true);
            roomRepository.save(room4);

            System.out.println("✅ Sample rooms created successfully!");
        }

        // Create admin user in both Keycloak and database
        if (!userRepository.existsByEmail("admin@meetingroom.com")) {
            try {
                // Create in Keycloak first
                String keycloakId = keycloakUserService.createKeycloakUser(
                    "admin@meetingroom.com",
                    "admin123",
                    "Admin",
                    "User"
                );

                // Then save to database
                User admin = new User();
                admin.setEmail("admin@meetingroom.com");
                admin.setName("Admin User");
                admin.setKeycloakId(keycloakId);
                admin.setRole(User.Role.ADMIN);
                userRepository.save(admin);

                System.out.println("✅ Admin user created in Keycloak and database");
                System.out.println("   Email: admin@meetingroom.com");
                System.out.println("   Password: admin123");
            } catch (Exception e) {
                System.err.println("⚠ Could not create admin user: " + e.getMessage());
                System.err.println("  You may need to create it manually via /api/auth/register");
            }
        }

        // Create test user in both Keycloak and database
        if (!userRepository.existsByEmail("user@test.com")) {
            try {
                // Create in Keycloak first
                String keycloakId = keycloakUserService.createKeycloakUser(
                    "user@test.com",
                    "password123",
                    "Test",
                    "User"
                );

                // Then save to database
                User user = new User();
                user.setEmail("user@test.com");
                user.setName("Test User");
                user.setKeycloakId(keycloakId);
                user.setRole(User.Role.USER);
                userRepository.save(user);

                System.out.println("✅ Test user created in Keycloak and database");
                System.out.println("   Email: user@test.com");
                System.out.println("   Password: password123");
            } catch (Exception e) {
                System.err.println("⚠ Could not create test user: " + e.getMessage());
                System.err.println("  You may need to create it manually via /api/auth/register");
            }
        }
    }
}