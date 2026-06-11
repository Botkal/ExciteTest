package com.excite.leavecalendar.dto;

import com.excite.leavecalendar.entity.LeaveStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class LeaveRequestDto {

    // ── Inbound (create) ──────────────────────────────────────────────────────

    public record CreateLeaveRequest(
            @NotNull(message = "Team member ID is required")
            Long teamMemberId,

            @NotNull(message = "Start date is required")
            LocalDate startDate,

            @NotNull(message = "End date is required")
            LocalDate endDate,

            @NotBlank(message = "Reason is required")
            String reason
    ) {}

    public record UpdateStatusRequest(
            @NotNull(message = "Status is required")
            LeaveStatus status
    ) {}

    // ── Outbound (responses) ──────────────────────────────────────────────────

    public record TeamMemberResponse(
            Long id,
            String name
    ) {}

    public record LeaveResponse(
            Long id,
            TeamMemberResponse teamMember,
            LocalDate startDate,
            LocalDate endDate,
            String reason,
            LeaveStatus status
    ) {}

    public record OnCallWeek(
            int year,
            int weekNumber,
            LocalDate weekStart,
            LocalDate weekEnd,
            TeamMemberResponse onCallMember,
            boolean hasConflict
    ) {}
}
