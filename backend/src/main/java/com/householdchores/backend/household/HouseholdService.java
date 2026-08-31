package com.householdchores.backend.household;

import com.householdchores.backend.user.User;
import com.householdchores.backend.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

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

    @Transactional(readOnly = true)
    public List<HouseholdResponse> getMyHouseholds(Jwt jwt) {
        User user = userService.getOrCreateUser(jwt);

        return householdMemberRepository.findByUserId(user.getId())
                .stream()
                .map(member -> new HouseholdResponse(
                        member.getHousehold().getId(),
                        member.getHousehold().getName(),
                        member.getHousehold().getTimezone(),
                        member.getRole()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HouseholdMemberResponse> getMembers(
            UUID householdId,
            Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());

        boolean isMember =
                householdMemberRepository
                        .existsByHouseholdIdAndUserId(householdId, userId);

        if (!isMember) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not a member of this household"
            );
        }

        return householdMemberRepository
                .findByHouseholdId(householdId)
                .stream()
                .map(member -> new HouseholdMemberResponse(
                        member.getUser().getId(),
                        member.getUser().getDisplayName(),
                        member.getRole(),
                        member.getStatus(),
                        member.getRotationPosition()
                ))
                .toList();
    }
}