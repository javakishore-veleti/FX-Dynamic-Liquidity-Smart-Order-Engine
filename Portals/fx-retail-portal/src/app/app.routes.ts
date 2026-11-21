import { Routes } from '@angular/router';
import { Shell } from './layout/shell';
import { DashboardPage } from './pages/dashboard';
import { ConvertPage } from './pages/convert';
import { AdvisorPage } from './pages/advisor';
import { PlannerPage } from './pages/planner';
import { TrackerPage } from './pages/tracker';

export const routes: Routes = [
  {
    path: '',
    component: Shell,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', component: DashboardPage },
      { path: 'convert', component: ConvertPage },
      { path: 'advisor', component: AdvisorPage },
      { path: 'planner', component: PlannerPage },
      { path: 'tracker', component: TrackerPage },
    ],
  },
  { path: '**', redirectTo: 'dashboard' },
];
