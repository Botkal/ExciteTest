package com.excite.leavecalendar.controller;

import com.excite.leavecalendar.dto.LeaveRequestDto.OnCallWeek;
import com.excite.leavecalendar.dto.LeaveRequestDto.TeamMemberResponse;
import com.excite.leavecalendar.service.OnCallService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/oncall")
@Tag(name = "On-Call Rotation", description = "View on-call schedule and conflicts")
public class OnCallController {

    private final OnCallService service;

    public OnCallController(OnCallService service) {
        this.service = service;
    }

    @GetMapping("/schedule")
    @Operation(summary = "Get on-call schedule for the next N weeks (default: 8)")
    public ResponseEntity<List<OnCallWeek>> getSchedule(
            @RequestParam(defaultValue = "8") int weeks) {
        if (weeks < 1 || weeks > 52) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(service.getSchedule(weeks));
    }

    @GetMapping("/today")
    @Operation(summary = "Get the on-call person for today")
    public ResponseEntity<TeamMemberResponse> getToday() {
        return ResponseEntity.ok(service.getOnCallForDate(LocalDate.now()));
    }
}
