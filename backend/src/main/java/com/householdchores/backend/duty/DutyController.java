package com.householdchores.backend.duty;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/households/{householdId}/duties")
public class DutyController {

    private final DutyService dutyService;

    public DutyController(DutyService dutyService) {
        this.dutyService = dutyService;
    }

    @GetMapping
    public List<DutyResponse> getUpcomingDuties(
            @PathVariable UUID householdId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return dutyService.getUpcomingDuties(
                householdId,
                startDate,
                endDate,
                UUID.fromString(jwt.getSubject())
        );
    }

    @GetMapping("/history")
    public List<DutyResponse> getHistory(
            @PathVariable UUID householdId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return dutyService.getHistory(
                householdId,
                startDate,
                endDate,
                UUID.fromString(jwt.getSubject())
        );
    }

    @PutMapping("/{dutyId}/resolve")
    public Duty resolveDuty(
            @PathVariable UUID householdId,
            @PathVariable UUID dutyId,
            @Valid @RequestBody ResolveDutyRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return dutyService.resolveDuty(
                householdId,
                dutyId,
                request.outcome(),
                request.completedByMemberId(),
                UUID.fromString(jwt.getSubject())
        );
    }

    @PostMapping("/{dutyId}/report")
    public DutyReport createReport(
            @PathVariable UUID householdId,
            @PathVariable UUID dutyId,
            @Valid @RequestBody CreateDutyReportRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return dutyService.createReport(
                householdId,
                dutyId,
                request.type(),
                request.otherMemberId(),
                request.note(),
                UUID.fromString(jwt.getSubject())
        );
    }
}