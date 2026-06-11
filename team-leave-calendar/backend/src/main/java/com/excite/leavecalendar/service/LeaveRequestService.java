package com.excite.leavecalendar.service;

import com.excite.leavecalendar.dto.LeaveRequestDto.*;
import com.excite.leavecalendar.entity.LeaveRequest;
import com.excite.leavecalendar.entity.LeaveStatus;
import com.excite.leavecalendar.entity.TeamMember;
import com.excite.leavecalendar.repository.LeaveRequestRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeaveRequestService {

    private final LeaveRequestRepository repository;
    private final TeamMemberService teamMemberService;

    public LeaveRequestService(LeaveRequestRepository repository,
                               TeamMemberService teamMemberService) {
        this.repository = repository;
        this.teamMemberService = teamMemberService;
    }

    public List<LeaveResponse> findAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<LeaveResponse> findByMember(Long memberId) {
        return repository.findByTeamMemberId(memberId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<LeaveResponse> findByStatus(LeaveStatus status) {
        return repository.findByStatus(status).stream()
                .map(this::toResponse)
                .toList();
    }

    public LeaveResponse create(CreateLeaveRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("End date must not be before start date.");
        }

        TeamMember member = teamMemberService.findEntityById(request.teamMemberId());

        List<LeaveRequest> overlaps = repository.findOverlapping(
                member.getId(), request.startDate(), request.endDate(), null);

        if (!overlaps.isEmpty()) {
            throw new IllegalStateException(
                    "Leave request overlaps with an existing request for " + member.getName() + ".");
        }

        LeaveRequest entity = new LeaveRequest();
        entity.setTeamMember(member);
        entity.setStartDate(request.startDate());
        entity.setEndDate(request.endDate());
        entity.setReason(request.reason());
        entity.setStatus(LeaveStatus.PENDING);

        return toResponse(repository.save(entity));
    }

    public LeaveResponse updateStatus(Long id, UpdateStatusRequest request) {
        LeaveRequest entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found with id: " + id));

        entity.setStatus(request.status());
        return toResponse(repository.save(entity));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Leave request not found with id: " + id);
        }
        repository.deleteById(id);
    }

    public LeaveResponse toResponse(LeaveRequest entity) {
        return new LeaveResponse(
                entity.getId(),
                teamMemberService.toResponse(entity.getTeamMember()),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getReason(),
                entity.getStatus()
        );
    }
}
