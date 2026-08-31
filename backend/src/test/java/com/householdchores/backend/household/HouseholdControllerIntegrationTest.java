package com.householdchores.backend.household;

import com.householdchores.backend.user.User;
import com.householdchores.backend.user.UserRepository;
import com.householdchores.backend.user.UserService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class HouseholdControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17")
                    .withInitScript("db/test-init.sql");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private HouseholdRepository householdRepository;

    @Autowired
    private HouseholdMemberRepository householdMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Test
    void createHousehold_withoutAuthentication_returns401()
            throws Exception {

        mockMvc.perform(
                        post("/api/households")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "Test Household",
                                          "timezone": "Europe/Copenhagen"
                                        }
                                        """)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createHousehold_withAuthentication_returns201AndCreatesOwner()
            throws Exception {

        UUID userId = UUID.randomUUID();

        jdbcTemplate.update(
                "INSERT INTO auth.users (id) VALUES (?)",
                userId
        );

        MvcResult result =  mockMvc.perform(
                        post("/api/households")
                                .with(jwt()
                                        .jwt(jwt -> jwt
                                                .subject(userId.toString())
                                                .claim(
                                                        "email",
                                                        "test@example.com"
                                                )
                                                .claim(
                                                        "user_metadata",
                                                        java.util.Map.of(
                                                                "display_name",
                                                                "Test User"
                                                        )
                                                )
                                        )
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "Test Household",
                                          "timezone": "Europe/Copenhagen"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andReturn();

        String householdId = JsonPath.read(
                result.getResponse().getContentAsString(),
                "$.id"
        );

        Household household = householdRepository
                .findById(UUID.fromString(householdId))
                .orElseThrow();

        assertThat(household.getName())
                .isEqualTo("Test Household");

        assertThat(household.getTimezone())
                .isEqualTo("Europe/Copenhagen");

        assertThat(householdMemberRepository.findByHouseholdId(household.getId()))
                .hasSize(1);

        HouseholdMember member =
                householdMemberRepository.findByHouseholdId(household.getId()).get(0);

        assertThat(member.getHousehold().getId())
                .isEqualTo(household.getId());

        assertThat(member.getRole())
                .isEqualTo(HouseholdMemberRole.OWNER);

        assertThat(member.getStatus())
                .isEqualTo(HouseholdMemberStatus.ACTIVE);

        assertThat(member.getRotationPosition())
                .isZero();

        assertThat(userRepository.findById(userId))
                .isPresent();
    }

    @Test
    void createHousehold_withBlankName_returns400()
            throws Exception {

        mockMvc.perform(
                        post("/api/households")
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "name": "",
                                      "timezone": "Europe/Copenhagen"
                                    }
                                    """)
                )
                .andExpect(status().isBadRequest());
    }


    @Test
    void createHousehold_withBlankTimezone_returns400()
            throws Exception {

        mockMvc.perform(
                        post("/api/households")
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "name": "Test Household",
                                      "timezone": ""
                                    }
                                    """)
                )
                .andExpect(status().isBadRequest());
    }


}