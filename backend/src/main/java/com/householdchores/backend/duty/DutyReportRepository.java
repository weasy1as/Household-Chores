package com.householdchores.backend.duty;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DutyReportRepository
        extends JpaRepository<DutyReport, UUID> {

    Optional<DutyReport> findByDutyId(UUID dutyId);
}