package com.householdchores.backend.duty;

import java.util.UUID;

public record DutyReportResponse(
        UUID reportedByMemberId,
        String reportedByMemberName,
        DutyReportType type,
        UUID otherMemberId,
        String otherMemberName,
        String note
) {
}