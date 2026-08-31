package com.householdchores.backend.household;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HouseholdMemberRepository
        extends JpaRepository<HouseholdMember, UUID> {
    List<HouseholdMember> findByUserId(UUID userId);
    List<HouseholdMember> findByHouseholdId(UUID householdId);
}