import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { OnCallWeek } from '../models/models';

@Injectable({ providedIn: 'root' })
export class OncallService {
  private readonly base = 'http://localhost:8080/api/oncall';

  constructor(private http: HttpClient) {}

  getSchedule(weeks: number = 8): Observable<OnCallWeek[]> {
    return this.http.get<OnCallWeek[]>(`${this.base}/schedule?weeks=${weeks}`);
  }
}
