import { Component, OnInit, TemplateRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NgbModal, NgbModalModule } from '@ng-bootstrap/ng-bootstrap';
import { LeaveRequest, LeaveStatus, TeamMember } from '../../models/models';
import { LeaveRequestService } from '../../services/leave-request.service';
import { TeamMemberService } from '../../services/team-member.service';

@Component({
  selector: 'app-leave-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, NgbModalModule],
  templateUrl: './leave-list.component.html'
})
export class LeaveListComponent implements OnInit {
  requests: LeaveRequest[] = [];
  members: TeamMember[] = [];
  filterMemberId: number | '' = '';
  filterStatus: LeaveStatus | '' = '';
  loading = true;
  error = '';
  successMsg = '';

  readonly statuses: LeaveStatus[] = ['PENDING', 'APPROVED', 'REJECTED'];

  constructor(
    private leaveService: LeaveRequestService,
    private memberService: TeamMemberService,
    private modal: NgbModal
  ) {}

  ngOnInit(): void {
    this.memberService.getAll().subscribe(m => this.members = m);
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = '';
    const mid = this.filterMemberId ? +this.filterMemberId : undefined;
    const st = this.filterStatus || undefined;
    this.leaveService.getAll(mid, st).subscribe({
      next: data => { this.requests = data; this.loading = false; },
      error: () => { this.error = 'Failed to load requests.'; this.loading = false; }
    });
  }

  pendingAction: { type: 'approve' | 'reject' | 'delete'; id: number } | null = null;

  confirmAction(type: 'approve' | 'reject' | 'delete', id: number, tpl: TemplateRef<unknown>): void {
    this.pendingAction = { type, id };
    this.modal.open(tpl, { centered: true, size: 'sm' }).result.then(
      () => this.runAction(),
      () => { this.pendingAction = null; }
    );
  }

  private runAction(): void {
    if (!this.pendingAction) return;
    const { type, id } = this.pendingAction;
    this.pendingAction = null;

    if (type === 'delete') {
      this.leaveService.delete(id).subscribe({
        next: () => { this.requests = this.requests.filter(r => r.id !== id); this.successMsg = 'Deleted.'; },
        error: () => this.error = 'Could not delete.'
      });
    } else {
      const status = type === 'approve' ? 'APPROVED' : 'REJECTED';
      this.leaveService.updateStatus(id, status).subscribe({
        next: updated => this.replace(updated),
        error: () => this.error = `Could not ${type}.`
      });
    }
  }

  private replace(updated: LeaveRequest): void {
    this.requests = this.requests.map(r => r.id === updated.id ? updated : r);
  }
}
