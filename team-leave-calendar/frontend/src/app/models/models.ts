export type LeaveStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface TeamMember {
  id: number;
  name: string;
}

export interface LeaveRequest {
  id: number;
  teamMember: TeamMember;
  startDate: string;
  endDate: string;
  reason: string;
  status: LeaveStatus;
}

export interface CreateLeaveRequest {
  teamMemberId: number;
  startDate: string;
  endDate: string;
  reason: string;
}

export interface OnCallWeek {
  year: number;
  weekNumber: number;
  weekStart: string;
  weekEnd: string;
  onCallMember: TeamMember;
  hasConflict: boolean;
}
