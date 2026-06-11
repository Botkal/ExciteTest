package com.excite.leavecalendar.repository;

import com.excite.leavecalendar.entity.LeaveRequest;
import com.excite.leavecalendar.entity.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByTeamMemberId(Long teamMemberId);

    List<LeaveRequest> findByStatus(LeaveStatus status);

    /**
     * Find overlapping leave requests for a team member.
     * Two ranges overlap if: startA <= endB AND endA >= startB
     * Optionally excludes a specific request ID (for future edit use).
     */
    @Query("""
            SELECT lr FROM LeaveRequest lr
            WHERE lr.teamMember.id = :memberId
              AND lr.startDate <= :endDate
              AND lr.endDate >= :startDate
              AND (:excludeId IS NULL OR lr.id <> :excludeId)
            """)
    List<LeaveRequest> findOverlapping(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludeId") Long excludeId
    );

    /**
     * Find approved leave requests for a specific member that overlap with a date range.
     * Used for on-call conflict detection.
     */
    @Query("""
            SELECT lr FROM LeaveRequest lr
            WHERE lr.teamMember.id = :memberId
              AND lr.status = com.excite.leavecalendar.entity.LeaveStatus.APPROVED
              AND lr.startDate <= :endDate
              AND lr.endDate >= :startDate
            """)
    List<LeaveRequest> findApprovedOverlapping(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
