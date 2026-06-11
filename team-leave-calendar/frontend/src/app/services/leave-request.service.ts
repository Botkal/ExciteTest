import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateLeaveRequest, LeaveRequest, LeaveStatus } from '../models/models';

@Injectable({ providedIn: 'root' })
export class LeaveRequestService {
  private readonly base = 'http://localhost:8080/api/leave-requests';

  constructor(private http: HttpClient) {}

  getAll(memberId?: number, status?: LeaveStatus): Observable<LeaveRequest[]> {
    let params = new HttpParams();
    if (memberId) params = params.set('memberId', memberId);
    if (status) params = params.set('status', status);
    return this.http.get<LeaveRequest[]>(this.base, { params });
  }

  create(request: CreateLeaveRequest): Observable<LeaveRequest> {
    return this.http.post<LeaveRequest>(this.base, request);
  }

  updateStatus(id: number, status: LeaveStatus): Observable<LeaveRequest> {
    return this.http.patch<LeaveRequest>(`${this.base}/${id}/status`, { status });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
