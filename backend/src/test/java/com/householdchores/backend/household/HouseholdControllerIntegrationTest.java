package com.householdchores.backend.household;

import com.householdchores.backend.support.IntegrationTestFixtures;
import com.householdchores.backend.support.TestUser;
import com.householdchores.backend.user.User;
import com.householdchores.backend.user.UserRepository;
import com.householdchores.backend.user.UserService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
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

    private IntegrationTestFixtures fixtures;

    @BeforeEach
    void setUp() {
        fixtures = new IntegrationTestFixtures(jdbcTemplate);
    }


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

    @Test
    void deleteHousehold_withoutAuthentication_returns401() throws Exception {

        UUID householdId = UUID.randomUUID();

        mockMvc.perform(
                        delete("/api/households/{householdId}", householdId)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteHousehold_asRegularMember_returns403() throws Exception {

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

        // Add the regular member
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

        // Regular member attempts to delete household
        mockMvc.perform(
                        delete(
                                "/api/households/{householdId}",
                                householdId
                        )
                                .with(memberJwt)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteHousehold_asOwner_returns204AndDeletesHousehold()
            throws Exception {

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

        UUID id = UUID.fromString(householdId);

        mockMvc.perform(
                        delete("/api/households/{householdId}", householdId)
                                .with(ownerJwt)
                )
                .andExpect(status().isNoContent());

        assertThat(householdRepository.findById(id))
                .isEmpty();
    }

    @Test
    void deleteHousehold_asOwner_deletesHouseholdMembers()
            throws Exception {

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

        UUID id = UUID.fromString(householdId);

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

        assertThat(
                householdMemberRepository.findByHouseholdId(id)
        ).hasSize(2);

        mockMvc.perform(
                        delete("/api/households/{householdId}", householdId)
                                .with(ownerJwt)
                )
                .andExpect(status().isNoContent());

        assertThat(
                householdMemberRepository.findByHouseholdId(id)
        ).isEmpty();

        assertThat(
                householdRepository.findById(id)
        ).isEmpty();
    }

    @Test
    void deleteHousehold_doesNotExist_returns404() throws Exception {

        TestUser owner = fixtures.createUser(
                "owner-" + UUID.randomUUID() + "@example.com",
                "Owner"
        );

        var ownerJwt = jwt().jwt(jwt -> jwt
                .subject(owner.id().toString())
                .claim("email", owner.email())
        );

        UUID householdId = UUID.randomUUID();

        mockMvc.perform(
                        delete("/api/households/{householdId}", householdId)
                                .with(ownerJwt)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteHousehold_ownerOfDifferentHousehold_returns403()
            throws Exception {

        TestUser ownerA = fixtures.createUser(
                "owner-a-" + UUID.randomUUID() + "@example.com",
                "Owner A"
        );

        TestUser ownerB = fixtures.createUser(
                "owner-b-" + UUID.randomUUID() + "@example.com",
                "Owner B"
        );

        var jwtA = jwt().jwt(jwt -> jwt
                .subject(ownerA.id().toString())
                .claim("email", ownerA.email())
        );

        var jwtB = jwt().jwt(jwt -> jwt
                .subject(ownerB.id().toString())
                .claim("email", ownerB.email())
        );

        // Owner A creates Household A
        mockMvc.perform(
                        post("/api/households")
                                .with(jwtA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                  "name": "Household A",
                                  "timezone": "Europe/Copenhagen"
                                }
                                """)
                )
                .andExpect(status().isCreated());

        // Owner B creates Household B
        MvcResult result = mockMvc.perform(
                        post("/api/households")
                                .with(jwtB)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                  "name": "Household B",
                                  "timezone": "Europe/Copenhagen"
                                }
                                """)
                )
                .andExpect(status().isCreated())
                .andReturn();

        String householdBId = JsonPath.read(
                result.getResponse().getContentAsString(),
                "$.id"
        );

        // Owner A tries to delete Household B
        mockMvc.perform(
                        delete("/api/households/{householdId}", householdBId)
                                .with(jwtA)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteHousehold_owner_returns204AndHouseholdIsDeleted()
            throws Exception {

        TestUser owner = fixtures.createUser(
                "delete-owner-" + UUID.randomUUID() + "@example.com",
                "Delete Owner"
        );

        var ownerJwt = jwt().jwt(jwt -> jwt
                .subject(owner.id().toString())
                .claim("email", owner.email())
        );

        MvcResult result = mockMvc.perform(
                        post("/api/households")
                                .with(ownerJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                  "name": "Delete Test Household",
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

        UUID householdUuid = UUID.fromString(householdId);

        mockMvc.perform(
                        delete("/api/households/{householdId}", householdId)
                                .with(ownerJwt)
                )
                .andExpect(status().isNoContent());

        assertThat(householdRepository.findById(householdUuid))
                .isEmpty();

        assertThat(
                householdMemberRepository.findByHouseholdId(householdUuid)
        ).isEmpty();
    }

    @Test
    void deleteHousehold_withMultipleMembers_deletesAllMembers()
            throws Exception {

        TestUser owner = fixtures.createUser(
                "owner-" + UUID.randomUUID() + "@example.com",
                "Owner"
        );

        TestUser member1 = fixtures.createUser(
                "member1-" + UUID.randomUUID() + "@example.com",
                "Member 1"
        );

        TestUser member2 = fixtures.createUser(
                "member2-" + UUID.randomUUID() + "@example.com",
                "Member 2"
        );

        var ownerJwt = jwt().jwt(jwt -> jwt
                .subject(owner.id().toString())
                .claim("email", owner.email())
        );

        MvcResult result = mockMvc.perform(
                        post("/api/households")
                                .with(ownerJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                  "name": "Delete Test Household",
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

        // Add member 1
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
                                """.formatted(member1.email()))
                )
                .andExpect(status().isCreated());

        // Add member 2
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
                                """.formatted(member2.email()))
                )
                .andExpect(status().isCreated());

        UUID householdUuid = UUID.fromString(householdId);

        // Verify we have 3 members before deletion
        assertThat(
                householdMemberRepository.findByHouseholdId(householdUuid)
        ).hasSize(3);

        // Delete household
        mockMvc.perform(
                        delete("/api/households/{householdId}", householdId)
                                .with(ownerJwt)
                )
                .andExpect(status().isNoContent());

        // Verify all members are gone
        assertThat(
                householdMemberRepository.findByHouseholdId(householdUuid)
        ).isEmpty();

        // Verify household is gone
        assertThat(
                householdRepository.findById(householdUuid)
        ).isEmpty();
    }


}