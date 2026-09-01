package com.householdchores.backend.duty;

import com.householdchores.backend.household.HouseholdMember;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "duty_reports")
public class DutyReport {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "duty_id", nullable = false, unique = true)
    private Duty duty;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reported_by_member_id", nullable = false)
    private HouseholdMember reportedByMember;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DutyReportType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "other_member_id")
    private HouseholdMember otherMember;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(nullable = false)
    private Instant createdAt;

    protected DutyReport() {
    }

    public DutyReport(
            Duty duty,
            HouseholdMember reportedByMember,
            DutyReportType type,
            HouseholdMember otherMember,
            String note
    ) {
        this.id = UUID.randomUUID();
        this.duty = duty;
        this.reportedByMember = reportedByMember;
        this.type = type;
        this.otherMember = otherMember;
        this.note = note;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Duty getDuty() {
        return duty;
    }

    public HouseholdMember getReportedByMember() {
        return reportedByMember;
    }

    public DutyReportType getType() {
        return type;
    }

    public HouseholdMember getOtherMember() {
        return otherMember;
    }

    public String getNote() {
        return note;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}