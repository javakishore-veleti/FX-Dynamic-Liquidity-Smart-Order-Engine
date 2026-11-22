import { Routes } from '@angular/router';
import { Shell } from './layout/shell';
import { DashboardPage } from './pages/dashboard';
import { TopologyPage } from './pages/topology';
import { IncidentRcaPage } from './pages/incident-rca';
import { PolicyStudioPage } from './pages/policy-studio';
import { SyntheticPage } from './pages/synthetic';
import { AgentsPage } from './pages/agents';
import { SreDashboardsPage } from './pages/sre-dashboards';
import { WorkflowsMasterPage } from './pages/workflows-master';
import { WorkflowsExecutionsPage } from './pages/workflows-executions';
import { RefLpBookPage } from './pages/ref-lp-book';
import { RefCalendarPage } from './pages/ref-calendar';
import { RefSettlementPage } from './pages/ref-settlement';
import { RefCreditPage } from './pages/ref-credit';
import { RefLpRankingsPage } from './pages/ref-lp-rankings';
import { RefSchedulesPage } from './pages/ref-schedules';
import { RefSchedulesCreatePage } from './pages/ref-schedules-create';
import { InitDataPage } from './pages/initdata/initdata';

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
      { path: 'initial-data', component: InitDataPage },
      { path: 'reference', pathMatch: 'full', redirectTo: 'reference/schedules' },
      { path: 'reference/schedules', component: RefSchedulesPage },
      { path: 'reference/schedules/create', component: RefSchedulesCreatePage },
      { path: 'reference/lp-book', component: RefLpBookPage },
      { path: 'reference/calendar', component: RefCalendarPage },
      { path: 'reference/settlement', component: RefSettlementPage },
      { path: 'reference/credit', component: RefCreditPage },
      { path: 'reference/lp-rankings', component: RefLpRankingsPage },
      { path: 'workflows', pathMatch: 'full', redirectTo: 'workflows/master' },
      { path: 'workflows/master', component: WorkflowsMasterPage },
      { path: 'workflows/executions', component: WorkflowsExecutionsPage },
      { path: 'synthetic', component: SyntheticPage },
      { path: 'agents', component: AgentsPage },
      { path: 'sre-dashboards', component: SreDashboardsPage },
    ],
  },
  { path: '**', redirectTo: 'dashboard' },
];
