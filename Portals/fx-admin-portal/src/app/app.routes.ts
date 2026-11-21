import { Routes } from '@angular/router';
import { Shell } from './layout/shell';
import { DashboardPage } from './pages/dashboard';
import { TopologyPage } from './pages/topology';
import { IncidentRcaPage } from './pages/incident-rca';
import { PolicyStudioPage } from './pages/policy-studio';
import { SyntheticPage } from './pages/synthetic';
import { AgentsPage } from './pages/agents';
import { SreDashboardsPage } from './pages/sre-dashboards';

export const routes: Routes = [
  {
    path: '',
    component: Shell,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', component: DashboardPage },
      { path: 'topology', component: TopologyPage },
      { path: 'incident-rca', component: IncidentRcaPage },
      { path: 'policy-studio', component: PolicyStudioPage },
      { path: 'synthetic', component: SyntheticPage },
      { path: 'agents', component: AgentsPage },
      { path: 'sre-dashboards', component: SreDashboardsPage },
    ],
  },
  { path: '**', redirectTo: 'dashboard' },
];
