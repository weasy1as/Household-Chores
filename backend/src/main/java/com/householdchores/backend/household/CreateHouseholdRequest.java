package com.householdchores.backend.household;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateHouseholdRequest(

        @NotBlank(message = "Household name is required")
        @Size(max = 100, message = "Household name must not exceed 100 characters")
        String name,

        @NotBlank(message = "Timezone is required")
        @Size(max = 50, message = "Timezone must not exceed 50 characters")
        String timezone

) {
}