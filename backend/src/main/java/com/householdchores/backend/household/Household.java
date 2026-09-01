package com.householdchores.backend.household;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "households")
public class Household {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String timezone;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "rotation_start_position", nullable = false)
    private int rotationStartPosition;
    @Column(name = "rotation_start_date", nullable = false)
    private LocalDate rotationStartDate;

    protected Household() {
    }

    public Household(String name, String timezone) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.timezone = timezone;
        this.createdAt = Instant.now();
        this.rotationStartDate= LocalDate.now();
        this.rotationStartPosition=0;
    }

    public void update(
            String name,
            String timezone
    ) {
        this.name = name;
        this.timezone = timezone;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTimezone() {
        return timezone;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
    public int getRotationStartPosition() {
        return rotationStartPosition;
    }
    public void setRotationStartPosition(int rotationStartPosition) {
        this.rotationStartPosition = rotationStartPosition;
    }

    public LocalDate getRotationStartDate() {
        return rotationStartDate;
    }


}