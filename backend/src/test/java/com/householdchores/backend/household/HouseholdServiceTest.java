package com.householdchores.backend.household;

import com.householdchores.backend.user.User;
import com.householdchores.backend.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HouseholdServiceTest {

    @Mock
    private HouseholdRepository householdRepository;

    @Mock
    private HouseholdMemberRepository householdMemberRepository;

    @Mock
    private UserService userService;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private HouseholdService householdService;

    @Test
    void createHousehold_createsHouseholdAndOwnerMembership() {
        UUID userId = UUID.randomUUID();

        User user = new User(
                userId,
                "test@example.com",
                "Test User"
        );

        when(userService.getOrCreateUser(jwt)).thenReturn(user);

        Household result = householdService.createHousehold(
                "My Household",
                "Europe/Copenhagen",
                jwt
        );

        assertThat(result.getName()).isEqualTo("My Household");
        assertThat(result.getTimezone()).isEqualTo("Europe/Copenhagen");

        verify(householdRepository).save(result);

        ArgumentCaptor<HouseholdMember> memberCaptor =
                ArgumentCaptor.forClass(HouseholdMember.class);

        verify(householdMemberRepository).save(memberCaptor.capture());

        HouseholdMember owner = memberCaptor.getValue();

        assertThat(owner.getHousehold()).isEqualTo(result);
        assertThat(owner.getUser()).isEqualTo(user);
        assertThat(owner.getRole()).isEqualTo(HouseholdMemberRole.OWNER);
        assertThat(owner.getStatus()).isEqualTo(HouseholdMemberStatus.ACTIVE);
        assertThat(owner.getRotationPosition()).isZero();
    }
}