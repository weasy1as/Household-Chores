package com.householdchores.backend.user;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User getOrCreateUser(Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        System.out.println("getOrCreateUser called for: " + userId);

        return userRepository.findById(userId)
                .orElseGet(() -> {
                    System.out.println("User not found. Creating user...");
                    return createUser(jwt, userId);
                });
    }

    private User createUser(Jwt jwt, UUID userId) {
        String email = jwt.getClaimAsString("email");

        if (email == null || email.isBlank()) {
            throw new IllegalStateException(
                    "Authenticated user does not have an email"
            );
        }

        String displayName = null;

        Object userMetadata = jwt.getClaim("user_metadata");

        if (userMetadata instanceof Map<?, ?> metadata) {
            Object value = metadata.get("display_name");

            if (value != null) {
                displayName = value.toString();
            }
        }

        User user = new User(
                userId,
                email,
                displayName
        );

        return userRepository.save(user);
    }
}