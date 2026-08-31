package com.householdchores.backend.household;

import com.householdchores.backend.user.User;
import com.householdchores.backend.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
class HouseholdIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17")
                    .withInitScript("db/test-init.sql");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private HouseholdService householdService;

    @Autowired
    private HouseholdRepository householdRepository;

    @Autowired
    private HouseholdMemberRepository householdMemberRepository;

    @Autowired
    private UserService userService;

    @Test
    void createHousehold_persistsHouseholdAndOwner() {
        UUID userId = UUID.randomUUID();

        Jwt jwt = mock(Jwt.class);

        when(jwt.getSubject()).thenReturn(userId.toString());
        when(jwt.getClaimAsString("email"))
                .thenReturn("test@example.com");
        when(jwt.getClaimAsString("user_metadata.display_name"))
                .thenReturn("Test User");

        jdbcTemplate.update(
                "INSERT INTO auth.users (id) VALUES (?)",
                userId
        );

        User user = userService.getOrCreateUser(jwt);

        Household household = householdService.createHousehold(
                "Test Household",
                "Europe/Copenhagen",
                jwt
        );

        assertThat(householdRepository.findById(household.getId()))
                .isPresent();

        assertThat(householdMemberRepository.findAll())
                .hasSize(1);

        HouseholdMember member =
                householdMemberRepository.findAll().get(0);

        assertThat(member.getHousehold().getId())
                .isEqualTo(household.getId());

        assertThat(member.getUser().getId())
                .isEqualTo(user.getId());

        assertThat(member.getRole())
                .isEqualTo(HouseholdMemberRole.OWNER);

        assertThat(member.getStatus())
                .isEqualTo(HouseholdMemberStatus.ACTIVE);

        assertThat(member.getRotationPosition())
                .isZero();
    }
}