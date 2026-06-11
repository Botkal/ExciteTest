import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TeamMember } from '../models/models';

@Injectable({ providedIn: 'root' })
export class TeamMemberService {
  private readonly base = 'http://localhost:8080/api/members';

  constructor(private http: HttpClient) {}

  getAll(): Observable<TeamMember[]> {
    return this.http.get<TeamMember[]>(this.base);
  }
}
