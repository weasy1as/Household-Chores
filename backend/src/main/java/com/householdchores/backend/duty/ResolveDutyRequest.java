package com.householdchores.backend.duty;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ResolveDutyRequest(

        @NotNull
        DutyOutcome outcome,

        UUID completedByMemberId

) {
}