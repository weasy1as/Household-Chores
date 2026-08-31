package com.householdchores.backend.household;

import com.householdchores.backend.user.User;
import com.householdchores.backend.user.UserRepository;
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
    private final UserRepository userRepository;

    public HouseholdService(
            HouseholdRepository householdRepository,
            HouseholdMemberRepository householdMemberRepository,
            UserService userService,
            UserRepository userRepository
    ) {
        this.householdRepository = householdRepository;
        this.householdMemberRepository = householdMemberRepository;
        this.userService = userService;
        this.userRepository=userRepository;
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
    @Transactional
    public HouseholdMember addMember(
            UUID householdId,
            String email,
            Jwt jwt
    ) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());

        HouseholdMember currentMember =
                householdMemberRepository
                        .findByHouseholdIdAndUserId(householdId, currentUserId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.FORBIDDEN,
                                "You are not a member of this household"
                        ));

        if (currentMember.getRole() != HouseholdMemberRole.OWNER) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the household owner can add members"
            );
        }

        User userToAdd = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        if (householdMemberRepository
                .existsByHouseholdIdAndUserId(householdId, userToAdd.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "User is already a member of this household"
            );
        }

        int nextRotationPosition =
                householdMemberRepository
                        .findByHouseholdId(householdId)
                        .stream()
                        .mapToInt(HouseholdMember::getRotationPosition)
                        .max()
                        .orElse(-1) + 1;

        Household household = householdRepository
                .findById(householdId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Household not found"
                ));

        HouseholdMember newMember = new HouseholdMember(
                household,
                userToAdd,
                HouseholdMemberRole.MEMBER,
                HouseholdMemberStatus.ACTIVE,
                nextRotationPosition
        );

        return householdMemberRepository.save(newMember);
    }

    @Transactional
    public void removeMember(
            UUID householdId,
            UUID userIdToRemove,
            Jwt jwt
    ) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());

        HouseholdMember currentMember =
                householdMemberRepository
                        .findByHouseholdIdAndUserId(householdId, currentUserId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.FORBIDDEN,
                                "You are not a member of this household"
                        ));

        if (currentMember.getRole() != HouseholdMemberRole.OWNER) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the household owner can remove members"
            );
        }

        HouseholdMember memberToRemove =
                householdMemberRepository
                        .findByHouseholdIdAndUserId(householdId, userIdToRemove)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Member not found"
                        ));

        if (memberToRemove.getRole() == HouseholdMemberRole.OWNER) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "The household owner cannot be removed"
            );
        }

        householdMemberRepository.delete(memberToRemove);
    }
}