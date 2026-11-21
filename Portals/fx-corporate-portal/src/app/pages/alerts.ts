import { Component } from '@angular/core';
import { API } from '../api';

@Component({
  selector: 'app-alerts',
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
      <div>
        <h1 class="h3 mb-1">Macro &amp; Liquidity Regime Alerts</h1>
        <p class="mb-0" style="color: var(--fx-ink-soft)">
          Proactive banners from quote session metadata · SSE {{ api.quote }}
        </p>
      </div>
      <span class="badge badge-warn">{{ urgentCount }} active</span>
    </div>

    @if (banner) {
      <div class="fx-card mb-3" style="border-color: var(--fx-gold); background: #fff8eb">
        <div class="d-flex align-items-start gap-3">
          <i class="bi bi-exclamation-triangle-fill" style="color: var(--fx-gold); font-size: 1.4rem"></i>
          <div>
            <div class="fw-bold">{{ banner.title }}</div>
            <div style="color: var(--fx-ink-soft)">{{ banner.body }}</div>
          </div>
        </div>
      </div>
    }

    <div class="fx-card">
      <h2 class="h5 mb-3">Alert feed</h2>
      @for (a of alerts; track a.id) {
        <div
          class="d-flex justify-content-between align-items-start border-bottom py-3"
          style="border-color: var(--fx-border) !important"
        >
          <div>
            <div class="fw-bold">{{ a.title }}</div>
            <div class="small" style="color: var(--fx-ink-soft)">{{ a.detail }}</div>
          </div>
          <span class="badge" [class]="a.urgent ? 'badge-warn' : 'badge-fx'">{{ a.when }}</span>
        </div>
      }
    </div>
  `,
})
export class AlertsPage {
  readonly api = API;
  get urgentCount(): number {
    return this.alerts.filter((a) => a.urgent).length;
  }
  readonly banner = {
    title: 'NY 5:00 PM rollover — spreads expected to widen',
    body: 'Lock EUR/USD and GBP/USD hedges before 16:45 local if payroll settlements are due T+2.',
  };
  readonly alerts = [
    {
      id: 1,
      title: 'FOMC decision window',
      detail: 'Session regime: elevated · recommend avoid large sweeps 30m either side.',
      when: 'Tomorrow',
      urgent: true,
    },
    {
      id: 2,
      title: 'NFP release',
      detail: 'USD pairs liquidity thin pre-print · TWAP preferred for >$10M.',
      when: 'Fri 08:30',
      urgent: true,
    },
    {
      id: 3,
      title: 'ECB speakers cluster',
      detail: 'EUR/USD micro-volatility up · credit utilization unchanged.',
      when: 'Wed',
      urgent: false,
    },
    {
      id: 4,
      title: 'London/NY overlap open',
      detail: 'Tightest composite spreads — favorable for block hedges.',
      when: 'Now',
      urgent: false,
    },
  ];
}
