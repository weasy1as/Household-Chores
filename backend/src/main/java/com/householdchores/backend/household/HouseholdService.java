package com.householdchores.backend.household;

import com.householdchores.backend.user.User;
import com.householdchores.backend.user.UserService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HouseholdService {

    private final HouseholdRepository householdRepository;
    private final HouseholdMemberRepository householdMemberRepository;
    private final UserService userService;

    public HouseholdService(
            HouseholdRepository householdRepository,
            HouseholdMemberRepository householdMemberRepository,
            UserService userService
    ) {
        this.householdRepository = householdRepository;
        this.householdMemberRepository = householdMemberRepository;
        this.userService = userService;
    }

    @Transactional
    public Household createHousehold(
            String name,
            String timezone,
            Jwt jwt
    ) {
        User user = userService.getOrCreateUser(jwt);

        Household household = new Household(name, timezone);

        householdRepository.save(household);

        HouseholdMember owner = new HouseholdMember(
                household,
                user,
                HouseholdMemberRole.OWNER,
                HouseholdMemberStatus.ACTIVE,
                0
        );

        householdMemberRepository.save(owner);

        return household;
    }
}