package com.excite.leavecalendar.controller;

import com.excite.leavecalendar.dto.LeaveRequestDto.TeamMemberResponse;
import com.excite.leavecalendar.service.TeamMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@Tag(name = "Team Members", description = "Manage team members")
public class TeamMemberController {

    private final TeamMemberService service;

    public TeamMemberController(TeamMemberService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all team members")
    public ResponseEntity<List<TeamMemberResponse>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }
}
