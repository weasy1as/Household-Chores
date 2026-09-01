package com.householdchores.backend.duty;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateDutyReportRequest(
        @NotNull DutyReportType type,
        @NotNull UUID otherMemberId,
        String note
) {
}