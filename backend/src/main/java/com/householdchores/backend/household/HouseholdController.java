package com.householdchores.backend.household;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

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
}