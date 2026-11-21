import { Routes } from '@angular/router';
import { Shell } from './layout/shell';
import { DashboardPage } from './pages/dashboard';
import { BlotterPage } from './pages/blotter';
import { ExecutionCopilotPage } from './pages/execution-copilot';
import { ExposuresPage } from './pages/exposures';
import { TcaPage } from './pages/tca';
import { AlertsPage } from './pages/alerts';

export const routes: Routes = [
  {
    path: '',
    component: Shell,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', component: DashboardPage },
      { path: 'blotter', component: BlotterPage },
      { path: 'execution-copilot', component: ExecutionCopilotPage },
      { path: 'exposures', component: ExposuresPage },
      { path: 'tca', component: TcaPage },
      { path: 'alerts', component: AlertsPage },
    ],
  },
  { path: '**', redirectTo: 'dashboard' },
];
