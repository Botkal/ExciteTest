import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgbDatepickerModule, NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { TeamMember } from '../../models/models';
import { LeaveRequestService } from '../../services/leave-request.service';
import { TeamMemberService } from '../../services/team-member.service';

@Component({
  selector: 'app-leave-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, NgbDatepickerModule],
  templateUrl: './leave-form.component.html'
})
export class LeaveFormComponent implements OnInit {
  form!: FormGroup;
  members: TeamMember[] = [];
  submitting = false;
  error = '';

  constructor(
    private fb: FormBuilder,
    private leaveService: LeaveRequestService,
    private memberService: TeamMemberService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      teamMemberId: [null, Validators.required],
      startDate: [null, Validators.required],
      endDate: [null, Validators.required],
      reason: ['', [Validators.required, Validators.minLength(3)]]
    }, { validators: this.dateRangeValidator });

    this.memberService.getAll().subscribe(m => this.members = m);
  }

  private dateRangeValidator(group: FormGroup) {
    const s = group.get('startDate')?.value as NgbDateStruct | null;
    const e = group.get('endDate')?.value as NgbDateStruct | null;
    if (!s || !e) return null;
    const start = new Date(s.year, s.month - 1, s.day);
    const end   = new Date(e.year, e.month - 1, e.day);
    return end < start ? { dateRange: true } : null;
  }

  private toDateString(d: NgbDateStruct): string {
    return `${d.year}-${String(d.month).padStart(2, '0')}-${String(d.day).padStart(2, '0')}`;
  }

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.submitting = true;
    this.error = '';

    const val = this.form.value;
    const payload = {
      teamMemberId: val.teamMemberId,
      startDate: this.toDateString(val.startDate),
      endDate: this.toDateString(val.endDate),
      reason: val.reason
    };

    this.leaveService.create(payload).subscribe({
      next: () => this.router.navigate(['/leaves']),
      error: err => {
        this.error = err.error?.error || 'Failed to create request.';
        this.submitting = false;
      }
    });
  }

  get f() { return this.form.controls; }
}
