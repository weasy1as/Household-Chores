package com.householdchores.backend.household;

import java.util.UUID;

public record HouseholdMemberResponse(
        UUID memberId,
        UUID userId,
        String displayName,
        String email,
        HouseholdMemberRole role,
        HouseholdMemberStatus status,
        int rotationPosition
) {
}