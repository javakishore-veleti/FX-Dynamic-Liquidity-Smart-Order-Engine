import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink],
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
      <div>
        <h1 class="h3 mb-1">Command Center</h1>
        <p class="mb-0" style="color: var(--fx-ink-soft)">
          Real-time governance for quote, qualify, route-exec, topology, and agent stacks.
        </p>
      </div>
      <div class="d-flex gap-2">
        <a routerLink="/synthetic" class="btn btn-fx-outline btn-sm">Run synthetic batch</a>
        <a routerLink="/incident-rca" class="btn btn-fx btn-sm">Open RCA copilot</a>
      </div>
    </div>

    <div class="row g-3 mb-4">
      @for (kpi of kpis; track kpi.label) {
        <div class="col-md-3">
          <div class="fx-card fx-kpi h-100">
            <span class="label">{{ kpi.label }}</span>
            <span class="value">{{ kpi.value }}</span>
            <span class="small" style="color: var(--fx-accent)">{{ kpi.hint }}</span>
          </div>
        </div>
      }
    </div>

    <div class="row g-3">
      <div class="col-lg-7">
        <div class="fx-card h-100">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h2 class="h5 mb-0">Service health</h2>
            <span class="badge badge-fx">Micrometer + OTEL</span>
          </div>
          <div class="table-responsive">
            <table class="table align-middle mb-0">
              <thead>
                <tr>
                  <th>Service</th>
                  <th>Port</th>
                  <th>p99</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                @for (row of services; track row.name) {
                  <tr>
                    <td class="fw-semibold">{{ row.name }}</td>
                    <td>{{ row.port }}</td>
                    <td>{{ row.p99 }}</td>
                    <td><span class="badge badge-fx">{{ row.status }}</span></td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        </div>
      </div>
      <div class="col-lg-5">
        <div class="fx-card h-100">
          <h2 class="h5 mb-3">Maker-checker queue</h2>
          @for (item of approvals; track item.id) {
            <div class="d-flex justify-content-between align-items-start border-bottom py-2" style="border-color: var(--fx-border) !important">
              <div>
                <div class="fw-bold">{{ item.title }}</div>
                <div class="small" style="color: var(--fx-ink-soft)">{{ item.detail }}</div>
              </div>
              <span class="badge" [class]="item.badgeClass">{{ item.state }}</span>
            </div>
          }
        </div>
      </div>
    </div>
  `,
})
export class DashboardPage {
  readonly kpis = [
    { label: 'Active workflows', value: '12', hint: '3 synthetic · 2 chaos' },
    { label: 'LP venues live', value: '18', hint: '1 halted (shadow)' },
    { label: 'Agent runs (24h)', value: '47', hint: 'RCA · blast-radius' },
    { label: 'Credit overrides', value: '2', hint: 'Awaiting second eye' },
  ];

  readonly services = [
    { name: 'fx-quote-service', port: 8081, p99: '4.2 ms', status: 'Healthy' },
    { name: 'fx-qualify-service', port: 8082, p99: '6.1 ms', status: 'Healthy' },
    { name: 'fx-route-exec-service', port: 8083, p99: '8.4 ms', status: 'Healthy' },
    { name: 'fx-admin-service', port: 8084, p99: '5.0 ms', status: 'Healthy' },
    { name: 'fx-topology-service', port: 8085, p99: '7.3 ms', status: 'Healthy' },
  ];

  readonly approvals = [
    {
      id: 1,
      title: 'Halt LP-CITI-ECN',
      detail: 'Toxicity spike · proposed by Incident RCA',
      state: 'Pending',
      badgeClass: 'badge-warn',
    },
    {
      id: 2,
      title: 'Credit override · CORP-441',
      detail: 'Temporary +15% USD headroom',
      state: 'Review',
      badgeClass: 'badge-fx',
    },
    {
      id: 3,
      title: 'DMN spread rule v12',
      detail: 'Shadow replay P&L +2.1 bps',
      state: 'Ready',
      badgeClass: 'badge-fx',
    },
  ];
}
