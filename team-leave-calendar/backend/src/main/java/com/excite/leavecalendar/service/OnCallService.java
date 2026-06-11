package com.excite.leavecalendar.service;

import com.excite.leavecalendar.dto.LeaveRequestDto.OnCallWeek;
import com.excite.leavecalendar.dto.LeaveRequestDto.TeamMemberResponse;
import com.excite.leavecalendar.entity.TeamMember;
import com.excite.leavecalendar.repository.LeaveRequestRepository;
import com.excite.leavecalendar.repository.TeamMemberRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
public class OnCallService {

    private final TeamMemberRepository teamMemberRepository;
    private final LeaveRequestRepository leaveRequestRepository;

    public OnCallService(TeamMemberRepository teamMemberRepository,
                         LeaveRequestRepository leaveRequestRepository) {
        this.teamMemberRepository = teamMemberRepository;
        this.leaveRequestRepository = leaveRequestRepository;
    }

    /**
     * Returns the on-call schedule for the given number of weeks,
     * starting from the Monday of the current week.
     */
    public List<OnCallWeek> getSchedule(int numberOfWeeks) {
        List<TeamMember> members = teamMemberRepository.findAll();
        if (members.isEmpty()) {
            return List.of();
        }

        List<OnCallWeek> schedule = new ArrayList<>();
        LocalDate weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // Anchor week 0 = ISO week 1 of year 2024 to ensure consistent rotation
        LocalDate anchor = LocalDate.of(2024, 1, 1)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        for (int i = 0; i < numberOfWeeks; i++) {
            LocalDate currentWeekStart = weekStart.plusWeeks(i);
            LocalDate currentWeekEnd = currentWeekStart.plusDays(6);

            long weeksSinceAnchor = anchor.until(currentWeekStart, java.time.temporal.ChronoUnit.WEEKS);
            int memberIndex = (int) Math.floorMod(weeksSinceAnchor, members.size());
            TeamMember onCallMember = members.get(memberIndex);

            boolean hasConflict = !leaveRequestRepository
                    .findApprovedOverlapping(onCallMember.getId(), currentWeekStart, currentWeekEnd)
                    .isEmpty();

            int year = currentWeekStart.get(IsoFields.WEEK_BASED_YEAR);
            int weekNumber = currentWeekStart.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);

            schedule.add(new OnCallWeek(
                    year,
                    weekNumber,
                    currentWeekStart,
                    currentWeekEnd,
                    new TeamMemberResponse(onCallMember.getId(), onCallMember.getName()),
                    hasConflict
            ));
        }

        return schedule;
    }

    /**
     * Returns the on-call member for a specific date.
     */
    public TeamMemberResponse getOnCallForDate(LocalDate date) {
        List<TeamMember> members = teamMemberRepository.findAll();
        if (members.isEmpty()) {
            throw new IllegalStateException("No team members found.");
        }

        LocalDate anchor = LocalDate.of(2024, 1, 1)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        long weeksSinceAnchor = anchor.until(weekStart, java.time.temporal.ChronoUnit.WEEKS);
        int memberIndex = (int) Math.floorMod(weeksSinceAnchor, members.size());
        TeamMember member = members.get(memberIndex);

        return new TeamMemberResponse(member.getId(), member.getName());
    }
}
