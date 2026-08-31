package com.householdchores.backend.household;

import com.householdchores.backend.user.User;
import com.householdchores.backend.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.jayway.jsonpath.JsonPath;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
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

    @Autowired
    private MockMvc mockMvc;

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

        assertThat(householdMemberRepository.findByHouseholdId(household.getId()))
                .hasSize(1);

        HouseholdMember member =
                householdMemberRepository.findByHouseholdId(household.getId()).get(0);

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

    //get household test
    @Test
    void getMyHouseholds_withoutAuthentication_returns401()
            throws Exception {

        mockMvc.perform(
                        get("/api/households")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMyHouseholds_withAuthenticatedUserWithoutHousehold_returnsEmptyList()
            throws Exception {

        UUID userId = UUID.randomUUID();

        mockMvc.perform(
                        get("/api/households")
                                .with(jwt().jwt(jwt -> jwt
                                        .subject(userId.toString())
                                        .claim("email", "test-" + userId + "@example.com")
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }



    @Test
    void getMyHouseholds_afterCreatingHousehold_returnsHousehold()
            throws Exception {

        UUID userId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO auth.users (id) VALUES (?)",
                userId
        );

        var jwtToken = jwt().jwt(jwt -> jwt
                .subject(userId.toString())
                .claim("email", "test-" + userId + "@example.com")
                .claim("user_metadata", Map.of(
                        "display_name", "Test User"
                ))
        );

        mockMvc.perform(
                        post("/api/households")
                                .with(jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "name": "Test Household",
                                      "timezone": "Europe/Copenhagen"
                                    }
                                    """)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        get("/api/households")
                                .with(jwtToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Test Household"))
                .andExpect(jsonPath("$[0].timezone").value("Europe/Copenhagen"))
                .andExpect(jsonPath("$[0].role").value("OWNER"));
    }

    @Test
    void getMembers_withoutAuthentication_returns401() throws Exception {

        UUID householdId = UUID.randomUUID();

        mockMvc.perform(
                        get("/api/households/{householdId}/members", householdId)
                )
                .andExpect(status().isUnauthorized());
    }
    @Test
    void getMembers_asHouseholdMember_returnsMembers() throws Exception {

        UUID userId = UUID.randomUUID();

        jdbcTemplate.update(
                "INSERT INTO auth.users (id) VALUES (?)",
                userId
        );

        var jwtToken = jwt().jwt(jwt -> jwt
                .subject(userId.toString())
                .claim("email", "test-" + userId + "@example.com")
                .claim("user_metadata", Map.of(
                        "display_name", "Test User"
                ))
        );


        MvcResult createResult = mockMvc.perform(
                        post("/api/households")
                                .with(jwtToken)
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

        String response = createResult
                .getResponse()
                .getContentAsString();

        String householdId = JsonPath.read(response, "$.id");

        mockMvc.perform(
                        get("/api/households/{householdId}/members", householdId)
                                .with(jwtToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$[0].displayName").value("Test User"))
                .andExpect(jsonPath("$[0].role").value("OWNER"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].rotationPosition").value(0));
    }
    @Test
    void getMembers_asNonMember_returns403() throws Exception {

        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        jdbcTemplate.update(
                "INSERT INTO auth.users (id) VALUES (?)",
                ownerId
        );

        jdbcTemplate.update(
                "INSERT INTO auth.users (id) VALUES (?)",
                otherUserId
        );

        var ownerJwt = jwt().jwt(jwt -> jwt
                .subject(ownerId.toString())
                .claim("email", "owner-" + ownerId + "@example.com")
                .claim("user_metadata", java.util.Map.of(
                        "display_name", "Owner"
                ))
        );

        MvcResult createResult = mockMvc.perform(
                        post("/api/households")
                                .with(ownerJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {
                          "name": "Owner Household",
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

        var otherUserJwt = jwt().jwt(jwt -> jwt
                .subject(otherUserId.toString())
                .claim("email", "other-" + otherUserId + "@example.com")
                .claim("user_metadata", java.util.Map.of(
                        "display_name", "Other User"
                ))
        );

        mockMvc.perform(
                        get("/api/households/{householdId}/members", householdId)
                                .with(otherUserJwt)
                )
                .andExpect(status().isForbidden());
    }
}