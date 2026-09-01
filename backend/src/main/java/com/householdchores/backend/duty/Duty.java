package com.householdchores.backend.duty;

import com.householdchores.backend.household.Household;
import com.householdchores.backend.household.HouseholdMember;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "duties")
public class Duty {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scheduled_member_id", nullable = false)
    private HouseholdMember scheduledMember;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private DutyStatus status;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private DutyOutcome outcome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completed_by_member_id")
    private HouseholdMember completedByMember;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    protected Duty() {
    }

    public Duty(
            Household household,
            LocalDate date,
            HouseholdMember scheduledMember
    ) {
        this.id = UUID.randomUUID();
        this.household = household;
        this.date = date;
        this.scheduledMember = scheduledMember;
        this.status = DutyStatus.PENDING_REVIEW;
    }

    public UUID getId() {
        return id;
    }

    public Household getHousehold() {
        return household;
    }

    public LocalDate getDate() {
        return date;
    }

    public HouseholdMember getScheduledMember() {
        return scheduledMember;
    }

    public DutyStatus getStatus() {
        return status;
    }

    public DutyOutcome getOutcome() {
        return outcome;
    }

    public HouseholdMember getCompletedByMember() {
        return completedByMember;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void resolve(
            DutyOutcome outcome,
            HouseholdMember completedByMember
    ) {
        this.outcome = outcome;
        this.completedByMember = completedByMember;
        this.status = DutyStatus.RESOLVED;
        this.resolvedAt = Instant.now();
    }
}