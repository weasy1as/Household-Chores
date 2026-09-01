package com.householdchores.backend.duty;

import java.time.LocalDate;
import java.util.UUID;

public record DutyResponse(
        UUID id,
        LocalDate date,
        UUID scheduledMemberId,
        String scheduledMemberName,
        DutyStatus status,
        DutyOutcome outcome,
        UUID completedByMemberId,
        String completedByMemberName,
        DutyReportResponse report
) {
}