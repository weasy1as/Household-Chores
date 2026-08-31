package com.householdchores.backend.household;

import com.householdchores.backend.support.IntegrationTestFixtures;
import com.householdchores.backend.support.TestUser;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class HouseholdMemberControllerIntegrationTest {

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
    private HouseholdMemberRepository householdMemberRepository;

    private IntegrationTestFixtures fixtures;

    @BeforeEach
    void setUp() {
        fixtures = new IntegrationTestFixtures(jdbcTemplate);
    }


    @Test
    void addMember_asOwner_returns201() throws Exception {
        TestUser owner = fixtures.createUser(
                "owner@example.com",
                "Owner"
        );

        TestUser member = fixtures.createUser(
                "member@example.com",
                "Member"
        );

        var ownerJwt = jwt().jwt(jwt -> jwt
                .subject(owner.id().toString())
                .claim("email", owner.email())
                .claim(
                        "user_metadata",
                        Map.of("display_name", owner.displayName())
                )
        );

        MvcResult createResult = mockMvc.perform(
                        post("/api/households")
                                .with(ownerJwt)
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
                createResult.getResponse().getContentAsString(),
                "$.id"
        );

        mockMvc.perform(
                        post("/api/households/{householdId}/members", householdId)
                                .with(ownerJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
    {
      "email": "%s"
    }
    """.formatted(member.email()))
                )
                .andExpect(status().isCreated());
    }
    @Test
    void addMember_withoutAuthentication_returns401() throws Exception {

        UUID householdId = UUID.randomUUID();

        mockMvc.perform(
                        post("/api/households/{householdId}/members", householdId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "email": "member@example.com"
                                    }
                                    """)
                )
                .andExpect(status().isUnauthorized());
    }
    @Test
    void addMember_asRegularMember_returns403() throws Exception {

        TestUser owner = fixtures.createUser(
                "owner-" + UUID.randomUUID() + "@example.com",
                "Owner"
        );

        TestUser member = fixtures.createUser(
                "member-" + UUID.randomUUID() + "@example.com",
                "Member"
        );

        TestUser newUser = fixtures.createUser(
                "new-user-" + UUID.randomUUID() + "@example.com",
                "New User"
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
                                      "name": "Test Household",
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
                        post("/api/households/{householdId}/members", householdId)
                                .with(ownerJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
    {
      "email": "%s"
    }
    """.formatted(member.email()))
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/households/{householdId}/members", householdId)
                                .with(memberJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
    {
      "email": "%s"
    }
    """.formatted(member.id()))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void addMember_userDoesNotExist_returns404() throws Exception {

        TestUser owner = fixtures.createUser(
                "owner-" + UUID.randomUUID() + "@example.com",
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
                                      "name": "Test Household",
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
                        post(
                                "/api/households/{householdId}/members",
                                householdId
                        )
                                .with(ownerJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "email": "does-not-exist@example.com"
                                    }
                                    """)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void addMember_userAlreadyMember_returns409() throws Exception {

        TestUser owner = fixtures.createUser(
                "owner-" + UUID.randomUUID() + "@example.com",
                "Owner"
        );

        TestUser member = fixtures.createUser(
                "member-" + UUID.randomUUID() + "@example.com",
                "Member"
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
                                      "name": "Test Household",
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

        // First addition succeeds
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

        // Second addition should fail
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
                .andExpect(status().isConflict());
    }

    @Test
    void removeMember_asOwner_returns204() throws Exception {

        TestUser owner = fixtures.createUser(
                "owner-" + UUID.randomUUID() + "@example.com",
                "Owner"
        );

        TestUser member = fixtures.createUser(
                "member-" + UUID.randomUUID() + "@example.com",
                "Member"
        );

        var ownerJwt = jwt().jwt(jwt -> jwt
                .subject(owner.id().toString())
                .claim("email", owner.email())
        );

        // Create household
        MvcResult createResult = mockMvc.perform(
                        post("/api/households")
                                .with(ownerJwt)
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
                createResult.getResponse().getContentAsString(),
                "$.id"
        );

        // Add member
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

        // Remove member
        mockMvc.perform(
                        delete(
                                "/api/households/{householdId}/members/{userId}",
                                householdId,
                                member.id()
                        )
                                .with(ownerJwt)
                )
                .andExpect(status().isNoContent());

        // Verify member is gone
        assertThat(
                householdMemberRepository
                        .findByHouseholdIdAndUserId(
                                UUID.fromString(householdId),
                                member.id()
                        )
        ).isEmpty();
    }

    @Test
    void removeMember_asRegularMember_returns403() throws Exception {

        TestUser owner = fixtures.createUser(
                "owner-" + UUID.randomUUID() + "@example.com",
                "Owner"
        );

        TestUser member = fixtures.createUser(
                "member-" + UUID.randomUUID() + "@example.com",
                "Member"
        );

        TestUser newUser = fixtures.createUser(
                "new-user-" + UUID.randomUUID() + "@example.com",
                "New User"
        );

        var ownerJwt = jwt().jwt(jwt -> jwt
                .subject(owner.id().toString())
                .claim("email", owner.email())
        );

        var memberJwt = jwt().jwt(jwt -> jwt
                .subject(member.id().toString())
                .claim("email", member.email())
        );

        // Create household
        MvcResult createResult = mockMvc.perform(
                        post("/api/households")
                                .with(ownerJwt)
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
                createResult.getResponse().getContentAsString(),
                "$.id"
        );

        // Add regular member
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

        // Regular member tries to remove another user
        mockMvc.perform(
                        delete(
                                "/api/households/{householdId}/members/{userId}",
                                householdId,
                                newUser.id()
                        )
                                .with(memberJwt)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void removeMember_ownerCannotRemoveThemselves_returns403() throws Exception {

        TestUser owner = fixtures.createUser(
                "owner-" + UUID.randomUUID() + "@example.com",
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
                                  "name": "Test Household",
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
                        delete(
                                "/api/households/{householdId}/members/{userId}",
                                householdId,
                                owner.id()
                        )
                                .with(ownerJwt)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void removeMember_userIsNotMember_returns404() throws Exception {

        TestUser owner = fixtures.createUser(
                "owner-" + UUID.randomUUID() + "@example.com",
                "Owner"
        );

        TestUser user = fixtures.createUser(
                "user-" + UUID.randomUUID() + "@example.com",
                "User"
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
                                  "name": "Test Household",
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
                        delete(
                                "/api/households/{householdId}/members/{userId}",
                                householdId,
                                user.id()
                        )
                                .with(ownerJwt)
                )
                .andExpect(status().isNotFound());
    }
}
