package com.householdchores.backend.household;

import java.util.UUID;

public record HouseholdMemberResponse(
        UUID userId,
        String displayName,
        HouseholdMemberRole role,
        HouseholdMemberStatus status,
        int rotationPosition
) {
}