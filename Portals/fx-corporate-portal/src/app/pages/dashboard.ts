import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { API } from '../api';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink],
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
      <div>
        <h1 class="h3 mb-1">Treasury Home</h1>
        <p class="mb-0" style="color: var(--fx-ink-soft)">
          Live depth, hedging, and best-execution for institutional flow.
        </p>
      </div>
      <div class="d-flex gap-2">
        <a routerLink="/execution-copilot" class="btn btn-fx btn-sm">Ask copilot</a>
        <a routerLink="/blotter" class="btn btn-fx-outline btn-sm">Open blotter</a>
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
          <h2 class="h5 mb-3">Open tickets</h2>
          <div class="table-responsive">
            <table class="table align-middle mb-0">
              <thead>
                <tr>
                  <th>Order</th>
                  <th>Pair</th>
                  <th>Notional</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                @for (o of orders; track o.id) {
                  <tr>
                    <td class="fw-semibold">{{ o.id }}</td>
                    <td>{{ o.pair }}</td>
                    <td>{{ o.notional }}</td>
                    <td><span class="badge badge-fx">{{ o.status }}</span></td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        </div>
      </div>
      <div class="col-lg-5">
        <div class="fx-card h-100">
          <h2 class="h5 mb-3">Service endpoints</h2>
          <div class="mb-2"><code>{{ api.quote }}</code> · quote SSE</div>
          <div class="mb-2"><code>{{ api.qualify }}</code> · credit / qualify</div>
          <div class="mb-3"><code>{{ api.routeExec }}</code> · route-exec</div>
          <a routerLink="/alerts" class="btn btn-fx-outline btn-sm">View regime alerts</a>
        </div>
      </div>
    </div>
  `,
})
export class DashboardPage {
  readonly api = API;
  readonly kpis = [
    { label: 'Credit headroom', value: '62%', hint: 'USD · CORP-441' },
    { label: 'Open hedges', value: '7', hint: '3 spot · 4 fwd' },
    { label: 'Avg savings', value: '1.8 bps', hint: 'vs mid · 30d TCA' },
    { label: 'Regime', value: 'Stable', hint: 'London/NY overlap' },
  ];
  readonly orders = [
    { id: 'CO-88421', pair: 'EUR/USD', notional: '$45M', status: 'Qualifying' },
    { id: 'CO-88418', pair: 'GBP/USD', notional: '$12M', status: 'Sweeping' },
    { id: 'CO-88410', pair: 'USD/JPY', notional: '$8M', status: 'Filled' },
  ];
}
