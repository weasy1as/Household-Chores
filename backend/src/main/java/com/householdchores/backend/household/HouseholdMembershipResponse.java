package com.householdchores.backend.household;

import java.time.LocalDate;
import java.util.UUID;

public record HouseholdMembershipResponse(
        UUID householdId,
        String householdName,
        String timezone,
        LocalDate rotationStartDate,
        int rotationStartPosition,
        HouseholdMemberRole role,
        HouseholdMemberStatus status,
        int rotationPosition
) {
}