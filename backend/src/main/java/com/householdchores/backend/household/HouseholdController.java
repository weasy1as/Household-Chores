package com.householdchores.backend.household;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/households")
public class HouseholdController {

    private final HouseholdService householdService;

    public HouseholdController(HouseholdService householdService) {
        this.householdService = householdService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Household createHousehold(
            @Valid @RequestBody CreateHouseholdRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return householdService.createHousehold(
                request.name(),
                request.timezone(),
                jwt
        );
    }

    @GetMapping
    public List<HouseholdResponse> getMyHouseholds(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return householdService.getMyHouseholds(jwt);
    }

    @GetMapping("/{householdId}/members")
    public List<HouseholdMemberResponse> getMembers(
            @PathVariable UUID householdId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return householdService.getMembers(householdId, jwt);
    }

    @PostMapping("/{householdId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public HouseholdMember addMember(
            @PathVariable UUID householdId,
            @RequestBody AddHouseholdMemberRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return householdService.addMember(
                householdId,
                request.email(),
                jwt
        );
    }

    @DeleteMapping("/{householdId}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(
            @PathVariable UUID householdId,
            @PathVariable UUID userId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        householdService.removeMember(
                householdId,
                userId,
                jwt
        );
    }
}