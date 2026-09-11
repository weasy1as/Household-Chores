package com.householdchores.backend.duty;

import java.time.LocalDate;
import java.util.UUID;

public record ScheduleEntryResponse(
        LocalDate date,
        UUID scheduledMemberId,
        String scheduledMemberName
) {
}