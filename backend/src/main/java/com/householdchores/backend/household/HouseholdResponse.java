package com.householdchores.backend.household;

import java.util.UUID;

public record HouseholdResponse(
        UUID id,
        String name,
        String timezone,
        HouseholdMemberRole role
) {
}