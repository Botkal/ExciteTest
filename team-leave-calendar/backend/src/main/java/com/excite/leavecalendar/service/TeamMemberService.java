package com.excite.leavecalendar.service;

import com.excite.leavecalendar.dto.LeaveRequestDto.TeamMemberResponse;
import com.excite.leavecalendar.entity.TeamMember;
import com.excite.leavecalendar.repository.TeamMemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamMemberService {

    private final TeamMemberRepository repository;

    public TeamMemberService(TeamMemberRepository repository) {
        this.repository = repository;
    }

    public List<TeamMemberResponse> findAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public TeamMember findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Team member not found with id: " + id));
    }

    public TeamMemberResponse toResponse(TeamMember member) {
        return new TeamMemberResponse(member.getId(), member.getName());
    }
}
