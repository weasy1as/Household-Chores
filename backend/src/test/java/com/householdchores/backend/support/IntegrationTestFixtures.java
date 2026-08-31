package com.householdchores.backend.support;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Map;
import java.util.UUID;

public class IntegrationTestFixtures {

    private final JdbcTemplate jdbcTemplate;

    public IntegrationTestFixtures(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public TestUser createUser(String email, String displayName) {

        UUID userId = UUID.randomUUID();

        jdbcTemplate.update(
                "INSERT INTO auth.users (id) VALUES (?)",
                userId
        );

        jdbcTemplate.update(
                """
                INSERT INTO users (id, email, display_name)
                VALUES (?, ?, ?)
                """,
                userId,
                email,
                displayName
        );

        return new TestUser(
                userId,
                email,
                displayName
        );
    }

    public Jwt springJwt(TestUser user) {
        return Jwt.withTokenValue("test-token-" + user.id())
                .header("alg", "none")
                .subject(user.id().toString())
                .claim("email", user.email())
                .claim(
                        "user_metadata",
                        Map.of(
                                "display_name",
                                user.displayName()
                        )
                )
                .build();
    }
}