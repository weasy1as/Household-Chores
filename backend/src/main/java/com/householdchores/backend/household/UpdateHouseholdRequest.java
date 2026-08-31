package com.householdchores.backend.household;

import jakarta.validation.constraints.NotBlank;

public record UpdateHouseholdRequest(
        @NotBlank String name,
        @NotBlank String timezone
) {
}