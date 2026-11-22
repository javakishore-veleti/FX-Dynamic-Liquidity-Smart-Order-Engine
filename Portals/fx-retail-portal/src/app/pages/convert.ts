import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { API } from '../api';

@Component({
  selector: 'app-convert',
  imports: [FormsModule],
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
      <div>
        <h1 class="h3 mb-1">All-In Rate Visualizer · FR-01</h1>
        <p class="mb-0" style="color: var(--fx-ink-soft)">
          Live retail-tier markup from quote {{ api.quote }}
        </p>
      </div>
      <button class="btn btn-fx btn-sm" (click)="refresh()">Refresh</button>
    </div>

    @if (error()) {
      <div class="alert alert-warning">{{ error() }}</div>
    }

    <div class="row g-3">
      <div class="col-lg-5">
        <div class="fx-card">
          <label class="form-label fw-bold">You send (USD notional)</label>
          <input type="number" class="form-control form-control-lg mb-3" [(ngModel)]="amount" />
          <label class="form-label fw-bold">Client tier</label>
          <select class="form-select mb-3" [(ngModel)]="tier" (ngModelChange)="refresh()">
            <option>TIER_2</option>
            <option>TIER_3</option>
            <option>TIER_1</option>
          </select>
          <div class="fx-kpi mb-3">
            <span class="label">Executable EUR (indicative)</span>
            <span class="value">{{ quote()?.executableRate ?? '—' }}</span>
          </div>
          <button class="btn btn-coral w-100" (click)="refresh()">Lock view (refresh quote)</button>
        </div>
      </div>
      <div class="col-lg-7">
        <div class="fx-card h-100">
          <h2 class="h5 mb-3">FR-01 rate breakdown</h2>
          <div class="mb-3">
            <div class="d-flex justify-content-between small fw-bold mb-1">
              <span>Session regime</span>
              <span>{{ quote()?.sessionRegime || regime()?.sessionRegime || '—' }}</span>
            </div>
            <div class="d-flex justify-content-between small fw-bold mb-1">
              <span>All-in markup (bps)</span>
              <span>{{ quote()?.allInSpreadBps ?? '—' }}</span>
            </div>
            <div class="d-flex justify-content-between small fw-bold mb-1">
              <span>Last-look penalty weight</span>
              <span>{{ regime()?.lastLookPenaltyWeight ?? '—' }}</span>
            </div>
            <div class="d-flex justify-content-between small fw-bold mb-1">
              <span>Macro active</span>
              <span>{{ regime()?.macroActive ?? '—' }}</span>
            </div>
          </div>
          <div class="d-flex flex-wrap gap-2">
            <span class="badge badge-fx">Wire fee: $0</span>
            <span class="badge badge-fx">Tier {{ tier }}</span>
            <span class="badge badge-coral">EURUSD spot</span>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class ConvertPage implements OnInit {
  private readonly http = inject(HttpClient);
  readonly api = API;
  amount = 2500;
  tier = 'TIER_3';
  readonly quote = signal<any | null>(null);
  readonly regime = signal<any | null>(null);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.error.set(null);
    this.http
      .get(`${API.quote}/api/v1/fx/regime/context`, {
        params: { clientTier: this.tier, ccyPair: 'EURUSD' },
      })
      .subscribe({
        next: (r) => this.regime.set(r),
        error: (err) => this.error.set(`Regime failed: ${err.message || err.status}`),
      });
    this.http
      .post(`${API.quote}/api/v1/fx/quotes/indicative`, {
        clientId: 'RETAIL-1',
        clientTier: this.tier,
        ccyPair: 'EURUSD',
        side: 'BUY',
        notional: this.amount,
        tenor: 'SPOT',
      })
      .subscribe({
        next: (q) => this.quote.set(q),
        error: (err) => this.error.set(`Indicative failed: ${err.message || err.status}`),
      });
  }
}
