package com.householdchores.backend.duty;

import com.householdchores.backend.household.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class DutyService {

    private final DutyRepository dutyRepository;
    private final HouseholdRepository householdRepository;
    private final HouseholdMemberRepository householdMemberRepository;
    private final DutyReportRepository dutyReportRepository;

    public DutyService(
            DutyRepository dutyRepository,
            HouseholdRepository householdRepository,
            HouseholdMemberRepository householdMemberRepository,
            DutyReportRepository dutyReportRepository
    ) {
        this.dutyRepository = dutyRepository;
        this.householdRepository = householdRepository;
        this.householdMemberRepository = householdMemberRepository;
        this.dutyReportRepository= dutyReportRepository;
    }

    @Transactional
    public Duty getOrCreateDuty(
            UUID householdId,
            LocalDate date
    ) {
        return dutyRepository
                .findByHouseholdIdAndDate(householdId, date)
                .orElseGet(() -> createDuty(householdId, date));
    }

    private Duty createDuty(
            UUID householdId,
            LocalDate date
    ) {
        Household household = householdRepository
                .findById(householdId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Household not found"
                ));

        List<HouseholdMember> activeMembers =
                householdMemberRepository
                        .findByHouseholdId(householdId)
                        .stream()
                        .filter(member ->
                                member.getStatus()== HouseholdMemberStatus.ACTIVE
                        )
                        .sorted(Comparator.comparing(
                                HouseholdMember::getRotationPosition
                        ))
                        .toList();

        if (activeMembers.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Household has no active members"
            );
        }

        HouseholdMember scheduledMember =
                activeMembers.get(
                        calculateRotationIndex(
                                activeMembers.size(),
                                household,
                                date
                        )
                );

        Duty duty = new Duty(
                household,
                date,
                scheduledMember
        );

        return dutyRepository.save(duty);
    }

    private int calculateRotationIndex(
            int memberCount,
            Household household,
            LocalDate date
    ) {
        long daysSinceStart =
                date.toEpochDay()
                        - household.getRotationStartDate().toEpochDay();

        return Math.floorMod(
                household.getRotationStartPosition()
                        + (int) daysSinceStart,
                memberCount
        );
    }

    @Transactional
    public Duty resolveDuty(
            UUID householdId,
            UUID dutyId,
            DutyOutcome outcome,
            UUID completedByMemberId,
            UUID currentUserId
    ) {
        Duty duty = dutyRepository
                .findById(dutyId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Duty not found"
                ));

        if (!duty.getHousehold().getId().equals(householdId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Duty not found"
            );
        }

        HouseholdMember currentMember =
                householdMemberRepository
                        .findByHouseholdIdAndUserId(
                                householdId,
                                currentUserId
                        )
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.FORBIDDEN,
                                "You are not a member of this household"
                        ));

        if (currentMember.getRole()
                != com.householdchores.backend.household.HouseholdMemberRole.OWNER) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the household owner can resolve duties"
            );
        }

        HouseholdMember completedByMember = null;

        if (outcome != DutyOutcome.MISSED) {

            if (completedByMemberId == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Completed by member is required"
                );
            }

            completedByMember =
                    householdMemberRepository
                            .findById(completedByMemberId)
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Completed by member not found"
                            ));

            if (!completedByMember
                    .getHousehold()
                    .getId()
                    .equals(householdId)) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Completed by member must belong to the household"
                );
            }
        }

        duty.resolve(outcome, completedByMember);
        dutyRepository.save(duty);

        if (outcome == DutyOutcome.MISSED) {
            createMakeupDuty(householdId, duty);
        }

        return duty;
    }

    private void createMakeupDuty(UUID householdId, Duty missedDuty) {
        LocalDate nextDay = missedDuty.getDate().plusDays(1);

        dutyRepository
                .findByHouseholdIdAndDate(householdId, nextDay)
                .ifPresentOrElse(
                        existingDuty -> {
                            if (existingDuty.getStatus() == DutyStatus.PENDING_REVIEW
                                    && existingDuty.getOutcome() == null) {
                                existingDuty = new Duty(
                                        missedDuty.getHousehold(),
                                        nextDay,
                                        missedDuty.getScheduledMember()
                                );
                                dutyRepository.save(existingDuty);
                            }
                        },
                        () -> {
                            Duty makeupDuty = new Duty(
                                    missedDuty.getHousehold(),
                                    nextDay,
                                    missedDuty.getScheduledMember()
                            );
                            dutyRepository.save(makeupDuty);
                        }
                );
    }

    @Transactional
    public DutyReport createReport(
            UUID householdId,
            UUID dutyId,
            DutyReportType type,
            UUID otherMemberId,
            String note,
            UUID currentUserId
    ) {
        Duty duty = dutyRepository
                .findById(dutyId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Duty not found"
                ));

        if (!duty.getHousehold().getId().equals(householdId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Duty not found"
            );
        }

        if (duty.getStatus() == DutyStatus.RESOLVED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Duty has already been resolved"
            );
        }

        HouseholdMember reportingMember =
                householdMemberRepository
                        .findByHouseholdIdAndUserId(
                                householdId,
                                currentUserId
                        )
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.FORBIDDEN,
                                "You are not a member of this household"
                        ));

        if (!duty.getScheduledMember().getId()
                .equals(reportingMember.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the scheduled member can report this duty"
            );
        }

        if (dutyReportRepository.findByDutyId(dutyId).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A report already exists for this duty"
            );
        }

        HouseholdMember otherMember =
                householdMemberRepository
                        .findById(otherMemberId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Member not found"
                        ));

        if (!otherMember.getHousehold().getId().equals(householdId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member must belong to the household"
            );
        }

        if (otherMember.getStatus() != HouseholdMemberStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Inactive members cannot be selected"
            );
        }

        DutyReport report = new DutyReport(
                duty,
                reportingMember,
                type,
                otherMember,
                note
        );

        return dutyReportRepository.save(report);
    }

    private void verifyHouseholdMember(
            UUID householdId,
            UUID currentUserId
    ) {
        boolean isMember =
                householdMemberRepository
                        .existsByHouseholdIdAndUserId(
                                householdId,
                                currentUserId
                        );

        if (!isMember) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not a member of this household"
            );
        }
    }
    @Transactional
    public List<DutyResponse> getUpcomingDuties(
            UUID householdId,
            LocalDate startDate,
            LocalDate endDate,
            UUID currentUserId
    ) {
        verifyHouseholdMember(householdId, currentUserId);

        return dutyRepository
                .findByHouseholdIdAndDateBetweenOrderByDateAsc(
                        householdId,
                        startDate,
                        endDate
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public List<DutyResponse> getHistory(
            UUID householdId,
            LocalDate startDate,
            LocalDate endDate,
            UUID currentUserId
    ) {
        verifyHouseholdMember(householdId, currentUserId);

        return dutyRepository
                .findByHouseholdIdAndDateBetweenOrderByDateAsc(
                        householdId,
                        startDate,
                        endDate
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private DutyResponse toResponse(Duty duty) {
        DutyReportResponse reportResponse = null;

        DutyReport report = dutyReportRepository.findByDutyId(duty.getId()).orElse(null);
        if (report != null) {
            reportResponse = new DutyReportResponse(
                    report.getReportedByMember().getUser().getId(),
                    report.getReportedByMember().getUser().getDisplayName(),
                    report.getType(),
                    report.getOtherMember() != null ? report.getOtherMember().getUser().getId() : null,
                    report.getOtherMember() != null ? report.getOtherMember().getUser().getDisplayName() : null,
                    report.getNote()
            );
        }

        String completedByMemberName = null;
        if (duty.getCompletedByMember() != null) {
            completedByMemberName = duty.getCompletedByMember().getUser().getDisplayName();
        }

        return new DutyResponse(
                duty.getId(),
                duty.getDate(),
                duty.getScheduledMember().getUser().getId(),
                duty.getScheduledMember().getUser().getDisplayName(),
                duty.getStatus(),
                duty.getOutcome(),
                duty.getCompletedByMember() != null ? duty.getCompletedByMember().getUser().getId() : null,
                completedByMemberName,
                reportResponse
        );
    }
}