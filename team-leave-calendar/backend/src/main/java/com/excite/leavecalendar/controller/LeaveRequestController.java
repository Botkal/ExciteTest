package com.excite.leavecalendar.controller;

import com.excite.leavecalendar.dto.LeaveRequestDto.*;
import com.excite.leavecalendar.entity.LeaveStatus;
import com.excite.leavecalendar.service.LeaveRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-requests")
@Tag(name = "Leave Requests", description = "Manage leave requests")
public class LeaveRequestController {

    private final LeaveRequestService service;

    public LeaveRequestController(LeaveRequestService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all leave requests, optionally filtered by member or status")
    public ResponseEntity<List<LeaveResponse>> getAll(
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) LeaveStatus status) {

        if (memberId != null) {
            return ResponseEntity.ok(service.findByMember(memberId));
        }
        if (status != null) {
            return ResponseEntity.ok(service.findByStatus(status));
        }
        return ResponseEntity.ok(service.findAll());
    }

    @PostMapping
    @Operation(summary = "Create a new leave request")
    public ResponseEntity<LeaveResponse> create(@Valid @RequestBody CreateLeaveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update the status of a leave request (Approve / Reject)")
    public ResponseEntity<LeaveResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(service.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a leave request")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
