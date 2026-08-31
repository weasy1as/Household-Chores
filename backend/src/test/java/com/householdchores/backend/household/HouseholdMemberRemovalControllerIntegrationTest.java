package com.householdchores.backend.household;

import com.householdchores.backend.support.IntegrationTestFixtures;
import com.householdchores.backend.support.TestUser;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class HouseholdMemberRemovalControllerIntegrationTest {

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
    void removeMember_asOwner_returns204AndRemovesMember()
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
                                      "email": "%s"
                                    }
                                    """.formatted(member.email()))
                )
                .andExpect(status().isCreated());

        UUID householdUuid = UUID.fromString(householdId);

        assertThat(
                householdMemberRepository.findByHouseholdId(householdUuid)
        ).hasSize(2);

        mockMvc.perform(
                        delete(
                                "/api/households/{householdId}/members/{userId}",
                                householdId,
                                member.id()
                        )
                                .with(ownerJwt)
                )
                .andExpect(status().isNoContent());

        assertThat(
                householdMemberRepository.findByHouseholdId(householdUuid)
        ).hasSize(1);

        assertThat(
                householdMemberRepository
                        .findByHouseholdIdAndUserId(
                                householdUuid,
                                member.id()
                        )
        ).isEmpty();
    }

    @Test
    void removeMember_asRegularMember_returns403()
            throws Exception {

        TestUser owner = fixtures.createUser(
                "owner-" + java.util.UUID.randomUUID() + "@example.com",
                "Owner"
        );

        TestUser member = fixtures.createUser(
                "member-" + java.util.UUID.randomUUID() + "@example.com",
                "Member"
        );

        TestUser target = fixtures.createUser(
                "target-" + java.util.UUID.randomUUID() + "@example.com",
                "Target"
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

        // Owner adds member
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

        // Owner adds target
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
                                """.formatted(target.email()))
                )
                .andExpect(status().isCreated());

        // Regular member tries to remove target
        mockMvc.perform(
                        delete(
                                "/api/households/{householdId}/members/{userId}",
                                householdId,
                                target.id()
                        )
                                .with(memberJwt)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void removeMember_userIsNotMember_returns404()
            throws Exception {

        TestUser owner = fixtures.createUser(
                "owner-" + java.util.UUID.randomUUID() + "@example.com",
                "Owner"
        );

        TestUser otherUser = fixtures.createUser(
                "other-" + java.util.UUID.randomUUID() + "@example.com",
                "Other User"
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
                                otherUser.id()
                        )
                                .with(ownerJwt)
                )
                .andExpect(status().isNotFound());
    }
}