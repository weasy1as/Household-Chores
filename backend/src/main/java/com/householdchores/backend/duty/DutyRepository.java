package com.householdchores.backend.duty;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DutyRepository extends JpaRepository<Duty, UUID> {

    Optional<Duty> findByHouseholdIdAndDate(
            UUID householdId,
            LocalDate date
    );

    List<Duty> findByHouseholdIdAndDateBetweenOrderByDateAsc(
            UUID householdId,
            LocalDate startDate,
            LocalDate endDate
    );
}