import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LeaveRequest, TeamMember } from '../../models/models';
import { LeaveRequestService } from '../../services/leave-request.service';
import { TeamMemberService } from '../../services/team-member.service';

interface CalDay {
  date: Date;
  currentMonth: boolean;
  isToday: boolean;
  events: LeaveRequest[];
}

@Component({
  selector: 'app-calendar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './calendar.component.html'
})
export class CalendarComponent implements OnInit {
  viewDate = new Date();
  weeks: CalDay[][] = [];
  allRequests: LeaveRequest[] = [];
  members: TeamMember[] = [];
  filterMemberId: number | '' = '';
  loading = true;
  readonly dayNames = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

  constructor(
    private leaveService: LeaveRequestService,
    private memberService: TeamMemberService
  ) {}

  ngOnInit(): void {
    this.memberService.getAll().subscribe(m => this.members = m);
    this.leaveService.getAll().subscribe({
      next: data => { this.allRequests = data; this.buildCalendar(); this.loading = false; },
      error: () => this.loading = false
    });
  }

  get title(): string {
    return this.viewDate.toLocaleDateString('en-GB', { month: 'long', year: 'numeric' });
  }

  prev(): void { this.viewDate = new Date(this.viewDate.getFullYear(), this.viewDate.getMonth() - 1, 1); this.buildCalendar(); }
  next(): void { this.viewDate = new Date(this.viewDate.getFullYear(), this.viewDate.getMonth() + 1, 1); this.buildCalendar(); }

  private buildCalendar(): void {
    const year = this.viewDate.getFullYear();
    const month = this.viewDate.getMonth();
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const today = new Date(); today.setHours(0,0,0,0);

    // ISO week: Monday = 0
    let startDow = firstDay.getDay() - 1; // JS: 0=Sun
    if (startDow < 0) startDow = 6;

    const days: CalDay[] = [];

    // Pad from previous month
    for (let i = startDow - 1; i >= 0; i--) {
      const d = new Date(year, month, -i);
      days.push({ date: d, currentMonth: false, isToday: false, events: [] });
    }

    // Current month days
    for (let d = 1; d <= lastDay.getDate(); d++) {
      const date = new Date(year, month, d);
      days.push({ date, currentMonth: true, isToday: date.getTime() === today.getTime(), events: [] });
    }

    // Pad to multiple of 7
    while (days.length % 7 !== 0) {
      const d = new Date(year, month + 1, days.length - lastDay.getDate() - startDow + 1);
      days.push({ date: d, currentMonth: false, isToday: false, events: [] });
    }

    // Assign leave requests to days
    const filtered = this.filterMemberId
      ? this.allRequests.filter(r => r.teamMember.id === +this.filterMemberId)
      : this.allRequests;

    for (const req of filtered) {
      const start = new Date(req.startDate);
      const end = new Date(req.endDate);
      for (const day of days) {
        if (day.date >= start && day.date <= end) {
          day.events.push(req);
        }
      }
    }

    this.weeks = [];
    for (let i = 0; i < days.length; i += 7) {
      this.weeks.push(days.slice(i, i + 7));
    }
  }

  onFilterChange(): void { this.buildCalendar(); }
}
