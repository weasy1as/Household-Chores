package com.householdchores.backend.household;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HouseholdMemberRepository
        extends JpaRepository<HouseholdMember, UUID> {
}