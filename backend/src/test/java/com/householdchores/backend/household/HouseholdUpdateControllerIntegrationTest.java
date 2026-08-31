package com.householdchores.backend.household;

import com.householdchores.backend.support.IntegrationTestFixtures;
import com.householdchores.backend.support.TestUser;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class HouseholdUpdateControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17")
                    .withInitScript("db/test-init.sql");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private IntegrationTestFixtures fixtures;

    @BeforeEach
    void setUp() {
        fixtures = new IntegrationTestFixtures(jdbcTemplate);
    }

    @Test
    void updateHousehold_asOwner_returns200WithUpdatedHousehold()
            throws Exception {

        TestUser owner = fixtures.createUser(
                "owner-" + java.util.UUID.randomUUID() + "@example.com",
                "Owner"
        );

        var ownerJwt = jwt().jwt(jwt -> jwt
                .subject(owner.id().toString())
                .claim("email", owner.email())
        );

        MvcResult createResult = mockMvc.perform(
                                post("/api/households")
                                .with(ownerJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "name": "Old Name",
                                      "timezone": "Europe/Copenhagen"
                                    }
                                    """)
                )
                .andExpect(status().isCreated())
                .andReturn();

        String householdId = JsonPath.read(
                createResult.getResponse().getContentAsString(),
                "$.id"
        );

        mockMvc.perform(
                        patch("/api/households/{householdId}", householdId)
                                .with(ownerJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "name": "New Name",
                                      "timezone": "Europe/Stockholm"
                                    }
                                    """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(householdId))
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.timezone").value("Europe/Stockholm"));
    }

    @Test
    void updateHousehold_asRegularMember_returns403()
            throws Exception {

        TestUser owner = fixtures.createUser(
                "owner-" + java.util.UUID.randomUUID() + "@example.com",
                "Owner"
        );

        TestUser member = fixtures.createUser(
                "member-" + java.util.UUID.randomUUID() + "@example.com",
                "Member"
        );

        var ownerJwt = jwt().jwt(jwt -> jwt
                .subject(owner.id().toString())
                .claim("email", owner.email())
        );

        var memberJwt = jwt().jwt(jwt -> jwt
                .subject(member.id().toString())
                .claim("email", member.email())
        );

        MvcResult createResult = mockMvc.perform(
                        post("/api/households")
                                .with(ownerJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                  "name": "Original Household",
                                  "timezone": "Europe/Copenhagen"
                                }
                                """)
                )
                .andExpect(status().isCreated())
                .andReturn();

        String householdId = JsonPath.read(
                createResult.getResponse().getContentAsString(),
                "$.id"
        );

        // Owner adds the regular member
        mockMvc.perform(
                        post(
                                "/api/households/{householdId}/members",
                                householdId
                        )
                                .with(ownerJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                  "email": "%s"
                                }
                                """.formatted(member.email()))
                )
                .andExpect(status().isCreated());

        // Regular member tries to update the household
        mockMvc.perform(
                        patch(
                                "/api/households/{householdId}",
                                householdId
                        )
                                .with(memberJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                  "name": "Hacked Household",
                                  "timezone": "Europe/Stockholm"
                                }
                                """)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void updateHousehold_withoutAuthentication_returns401()
            throws Exception {

        UUID householdId = UUID.randomUUID();

        mockMvc.perform(
                        patch(
                                "/api/households/{householdId}",
                                householdId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                  "name": "Updated Household",
                                  "timezone": "Europe/Stockholm"
                                }
                                """)
                )
                .andExpect(status().isUnauthorized());
    }
}