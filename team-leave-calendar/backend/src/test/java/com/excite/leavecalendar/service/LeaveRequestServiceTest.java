package com.excite.leavecalendar.service;

import com.excite.leavecalendar.dto.LeaveRequestDto.*;
import com.excite.leavecalendar.entity.LeaveRequest;
import com.excite.leavecalendar.entity.LeaveStatus;
import com.excite.leavecalendar.entity.TeamMember;
import com.excite.leavecalendar.repository.LeaveRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveRequestServiceTest {

    @Mock
    private LeaveRequestRepository repository;

    @Mock
    private TeamMemberService teamMemberService;

    @InjectMocks
    private LeaveRequestService service;

    private TeamMember alice;

    @BeforeEach
    void setUp() {
        alice = new TeamMember("Alice");
        alice.setId(1L);
    }

    @Test
    void create_shouldSucceed_whenNoOverlap() {
        CreateLeaveRequest request = new CreateLeaveRequest(
                1L,
                LocalDate.of(2025, 7, 1),
                LocalDate.of(2025, 7, 5),
                "Summer vacation"
        );

        when(teamMemberService.findEntityById(1L)).thenReturn(alice);
        when(repository.findOverlapping(eq(1L), any(), any(), isNull())).thenReturn(List.of());

        LeaveRequest saved = new LeaveRequest();
        saved.setId(1L);
        saved.setTeamMember(alice);
        saved.setStartDate(request.startDate());
        saved.setEndDate(request.endDate());
        saved.setReason(request.reason());
        saved.setStatus(LeaveStatus.PENDING);

        when(repository.save(any())).thenReturn(saved);
        when(teamMemberService.toResponse(alice)).thenReturn(new TeamMemberResponse(1L, "Alice"));

        LeaveResponse response = service.create(request);

        assertThat(response.status()).isEqualTo(LeaveStatus.PENDING);
        assertThat(response.teamMember().name()).isEqualTo("Alice");
        verify(repository).save(any());
    }

    @Test
    void create_shouldThrow_whenOverlapExists() {
        CreateLeaveRequest request = new CreateLeaveRequest(
                1L,
                LocalDate.of(2025, 7, 1),
                LocalDate.of(2025, 7, 5),
                "Vacation"
        );

        when(teamMemberService.findEntityById(1L)).thenReturn(alice);

        LeaveRequest existing = new LeaveRequest();
        existing.setId(99L);
        when(repository.findOverlapping(eq(1L), any(), any(), isNull()))
                .thenReturn(List.of(existing));

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("overlaps");

        verify(repository, never()).save(any());
    }

    @Test
    void create_shouldThrow_whenEndDateBeforeStartDate() {
        CreateLeaveRequest request = new CreateLeaveRequest(
                1L,
                LocalDate.of(2025, 7, 10),
                LocalDate.of(2025, 7, 5),
                "Bad dates"
        );

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End date must not be before start date");

        verifyNoInteractions(repository);
    }

    @Test
    void create_shouldSucceed_forSingleDayLeave() {
        LocalDate day = LocalDate.of(2025, 8, 15);
        CreateLeaveRequest request = new CreateLeaveRequest(1L, day, day, "Day off");

        when(teamMemberService.findEntityById(1L)).thenReturn(alice);
        when(repository.findOverlapping(eq(1L), any(), any(), isNull())).thenReturn(List.of());

        LeaveRequest saved = new LeaveRequest();
        saved.setId(2L);
        saved.setTeamMember(alice);
        saved.setStartDate(day);
        saved.setEndDate(day);
        saved.setReason("Day off");
        saved.setStatus(LeaveStatus.PENDING);

        when(repository.save(any())).thenReturn(saved);
        when(teamMemberService.toResponse(alice)).thenReturn(new TeamMemberResponse(1L, "Alice"));

        LeaveResponse response = service.create(request);

        assertThat(response.startDate()).isEqualTo(response.endDate());
    }

    @Test
    void updateStatus_shouldChangeStatusToApproved() {
        LeaveRequest entity = new LeaveRequest();
        entity.setId(1L);
        entity.setTeamMember(alice);
        entity.setStartDate(LocalDate.of(2025, 7, 1));
        entity.setEndDate(LocalDate.of(2025, 7, 5));
        entity.setReason("Vacation");
        entity.setStatus(LeaveStatus.PENDING);

        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(teamMemberService.toResponse(alice)).thenReturn(new TeamMemberResponse(1L, "Alice"));

        LeaveResponse response = service.updateStatus(1L, new UpdateStatusRequest(LeaveStatus.APPROVED));

        assertThat(response.status()).isEqualTo(LeaveStatus.APPROVED);
    }

    @Test
    void updateStatus_shouldThrow_whenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateStatus(999L, new UpdateStatusRequest(LeaveStatus.APPROVED)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void delete_shouldThrow_whenNotFound() {
        when(repository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(999L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
