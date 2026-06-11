import { Routes } from '@angular/router';
import { LeaveListComponent } from './pages/leave-list/leave-list.component';
import { LeaveFormComponent } from './pages/leave-form/leave-form.component';
import { CalendarComponent } from './pages/calendar/calendar.component';
import { OncallComponent } from './pages/oncall/oncall.component';

export const routes: Routes = [
  { path: '', redirectTo: 'leaves', pathMatch: 'full' },
  { path: 'leaves', component: LeaveListComponent },
  { path: 'leaves/new', component: LeaveFormComponent },
  { path: 'calendar', component: CalendarComponent },
  { path: 'oncall', component: OncallComponent },
];
