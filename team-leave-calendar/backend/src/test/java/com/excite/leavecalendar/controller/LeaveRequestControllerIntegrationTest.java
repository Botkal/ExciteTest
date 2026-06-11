package com.excite.leavecalendar.controller;

import com.excite.leavecalendar.entity.LeaveStatus;
import com.excite.leavecalendar.entity.TeamMember;
import com.excite.leavecalendar.repository.LeaveRequestRepository;
import com.excite.leavecalendar.repository.TeamMemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LeaveRequestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    private Long aliceId;

    @BeforeEach
    void setUp() {
        leaveRequestRepository.deleteAll();
        teamMemberRepository.deleteAll();
        TeamMember alice = teamMemberRepository.save(new TeamMember("Alice"));
        aliceId = alice.getId();
    }

    @Test
    void createLeaveRequest_shouldReturn201() throws Exception {
        Map<String, Object> body = Map.of(
                "teamMemberId", aliceId,
                "startDate", "2025-08-01",
                "endDate", "2025-08-05",
                "reason", "Summer vacation"
        );

        mockMvc.perform(post("/api/leave-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.teamMember.name").value("Alice"));
    }

    @Test
    void createLeaveRequest_shouldReturn409_whenOverlap() throws Exception {
        Map<String, Object> first = Map.of(
                "teamMemberId", aliceId,
                "startDate", "2025-08-01",
                "endDate", "2025-08-10",
                "reason", "Vacation"
        );
        mockMvc.perform(post("/api/leave-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        Map<String, Object> overlapping = Map.of(
                "teamMemberId", aliceId,
                "startDate", "2025-08-05",
                "endDate", "2025-08-15",
                "reason", "Another vacation"
        );
        mockMvc.perform(post("/api/leave-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overlapping)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void createLeaveRequest_shouldReturn400_whenEndBeforeStart() throws Exception {
        Map<String, Object> body = Map.of(
                "teamMemberId", aliceId,
                "startDate", "2025-08-10",
                "endDate", "2025-08-01",
                "reason", "Bad dates"
        );

        mockMvc.perform(post("/api/leave-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatus_shouldReturnApproved() throws Exception {
        Map<String, Object> createBody = Map.of(
                "teamMemberId", aliceId,
                "startDate", "2025-09-01",
                "endDate", "2025-09-05",
                "reason", "Holiday"
        );

        String response = mockMvc.perform(post("/api/leave-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBody)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(patch("/api/leave-requests/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "APPROVED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getAll_shouldReturnList() throws Exception {
        mockMvc.perform(get("/api/leave-requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getMembers_shouldReturnAlice() throws Exception {
        mockMvc.perform(get("/api/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alice"));
    }
}
