package com.householdchores.backend.household;

import com.householdchores.backend.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "household_members")
public class HouseholdMember {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private HouseholdMemberRole role;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private HouseholdMemberStatus status;
    @Column(name = "rotation_position", nullable = false)
    private int rotationPosition;

    protected HouseholdMember() {
    }

    public HouseholdMember(
            Household household,
            User user,
            HouseholdMemberRole role,
            HouseholdMemberStatus status,
            int rotationPosition
    ) {
        this.id = UUID.randomUUID();
        this.household = household;
        this.user = user;
        this.role = role;
        this.status = status;
        this.rotationPosition = rotationPosition;
    }

    public UUID getId() {
        return id;
    }

    public Household getHousehold() {
        return household;
    }

    public User getUser() {
        return user;
    }

    public HouseholdMemberRole getRole() {
        return role;
    }

    public HouseholdMemberStatus getStatus() {
        return status;
    }

    public int getRotationPosition() {
        return rotationPosition;
    }

    public void updateStatus(HouseholdMemberStatus status) {
        this.status = status;
    }
}