package com.householdchores.backend.household;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateRotationPositionRequest(
        @NotNull UUID userId,
        @NotNull Integer position
) {
}
