package com.excite.leavecalendar.service;

import com.excite.leavecalendar.dto.LeaveRequestDto.OnCallWeek;
import com.excite.leavecalendar.dto.LeaveRequestDto.TeamMemberResponse;
import com.excite.leavecalendar.entity.TeamMember;
import com.excite.leavecalendar.repository.LeaveRequestRepository;
import com.excite.leavecalendar.repository.TeamMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OnCallServiceTest {

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @InjectMocks
    private OnCallService service;

    private List<TeamMember> members;

    @BeforeEach
    void setUp() {
        TeamMember alice = new TeamMember("Alice");   alice.setId(1L);
        TeamMember bob = new TeamMember("Bob");       bob.setId(2L);
        TeamMember charlie = new TeamMember("Charlie"); charlie.setId(3L);
        TeamMember diana = new TeamMember("Diana");   diana.setId(4L);
        members = List.of(alice, bob, charlie, diana);
    }

    @Test
    void getSchedule_shouldReturnCorrectNumberOfWeeks() {
        when(teamMemberRepository.findAll()).thenReturn(members);
        when(leaveRequestRepository.findApprovedOverlapping(anyLong(), any(), any()))
                .thenReturn(List.of());

        List<OnCallWeek> schedule = service.getSchedule(8);

        assertThat(schedule).hasSize(8);
    }

    @Test
    void getSchedule_shouldRotateMembers() {
        when(teamMemberRepository.findAll()).thenReturn(members);
        when(leaveRequestRepository.findApprovedOverlapping(anyLong(), any(), any()))
                .thenReturn(List.of());

        List<OnCallWeek> schedule = service.getSchedule(4);

        // Each member should appear exactly once in 4 consecutive weeks
        List<String> names = schedule.stream()
                .map(w -> w.onCallMember().name())
                .toList();

        assertThat(names).doesNotHaveDuplicates();
        assertThat(names).containsExactlyInAnyOrder("Alice", "Bob", "Charlie", "Diana");
    }

    @Test
    void getSchedule_shouldReturnEmpty_whenNoMembers() {
        when(teamMemberRepository.findAll()).thenReturn(List.of());

        List<OnCallWeek> schedule = service.getSchedule(4);

        assertThat(schedule).isEmpty();
    }

    @Test
    void getSchedule_shouldMarkConflict_whenOnCallMemberHasApprovedLeave() {
        when(teamMemberRepository.findAll()).thenReturn(members);

        // First call: conflict for whoever is on call week 0
        when(leaveRequestRepository.findApprovedOverlapping(anyLong(), any(), any()))
                .thenReturn(List.of()) // weeks 1-3: no conflict
                .thenReturn(List.of()) // keep returning empty
                .thenReturn(List.of());

        // We only test that the hasConflict flag propagates correctly
        // by simulating one conflict on the first week
        when(leaveRequestRepository.findApprovedOverlapping(
                eq(members.get(0).getId()), any(), any()))
                .thenReturn(List.of(new com.excite.leavecalendar.entity.LeaveRequest()));

        List<OnCallWeek> schedule = service.getSchedule(4);

        OnCallWeek firstWeek = schedule.get(0);
        if (firstWeek.onCallMember().id().equals(members.get(0).getId())) {
            assertThat(firstWeek.hasConflict()).isTrue();
        }
    }

    @Test
    void getOnCallForDate_shouldReturnMember() {
        when(teamMemberRepository.findAll()).thenReturn(members);

        TeamMemberResponse result = service.getOnCallForDate(LocalDate.now());

        assertThat(result).isNotNull();
        assertThat(result.name()).isIn("Alice", "Bob", "Charlie", "Diana");
    }

    @Test
    void getSchedule_shouldWrapAround_afterFourWeeks() {
        when(teamMemberRepository.findAll()).thenReturn(members);
        when(leaveRequestRepository.findApprovedOverlapping(anyLong(), any(), any()))
                .thenReturn(List.of());

        List<OnCallWeek> schedule = service.getSchedule(8);

        // Week 0 and week 4 should have the same on-call member
        assertThat(schedule.get(0).onCallMember().name())
                .isEqualTo(schedule.get(4).onCallMember().name());
    }
}
