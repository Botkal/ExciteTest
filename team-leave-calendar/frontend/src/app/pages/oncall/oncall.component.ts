import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OnCallWeek } from '../../models/models';
import { OncallService } from '../../services/oncall.service';

@Component({
  selector: 'app-oncall',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './oncall.component.html'
})
export class OncallComponent implements OnInit {
  schedule: OnCallWeek[] = [];
  loading = true;
  error = '';

  constructor(private oncallService: OncallService) {}

  ngOnInit(): void {
    this.oncallService.getSchedule(12).subscribe({
      next: data => { this.schedule = data; this.loading = false; },
      error: () => { this.error = 'Failed to load on-call schedule.'; this.loading = false; }
    });
  }

  isCurrentWeek(week: OnCallWeek): boolean {
    const today = new Date().toISOString().split('T')[0];
    return week.weekStart <= today && today <= week.weekEnd;
  }
}
