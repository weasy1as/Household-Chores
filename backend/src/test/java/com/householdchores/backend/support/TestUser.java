package com.householdchores.backend.support;

import java.util.UUID;

public record TestUser(
        UUID id,
        String email,
        String displayName
) {
}