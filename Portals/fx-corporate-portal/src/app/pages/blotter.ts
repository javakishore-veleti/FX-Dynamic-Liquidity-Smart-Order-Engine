import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { API } from '../api';

type Indicative = {
  quoteId: string;
  ccyPair: string;
  side: string;
  executableRate: number;
  allInSpreadBps: number;
  sessionRegime: string;
  clientTier: string;
  quoteTtlMs: number;
};

type RegimeContext = {
  sessionRegime: string;
  macroActive: boolean;
  lastLookPenaltyWeight: number;
  sampleMarkupBps: number;
};

@Component({
  selector: 'app-blotter',
  imports: [FormsModule],
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
      <div>
        <h1 class="h3 mb-1">Multi-LP Depth Blotter · FR-01</h1>
        <p class="mb-0" style="color: var(--fx-ink-soft)">
          Live indicative from quote {{ api.quote }} · session markup applied
        </p>
      </div>
      <div class="d-flex gap-2 align-items-center">
        <select class="form-select form-select-sm" style="width: auto" [(ngModel)]="tier">
          <option>TIER_1</option>
          <option>TIER_2</option>
          <option>TIER_3</option>
        </select>
        <select class="form-select form-select-sm" style="width: auto" [(ngModel)]="pair">
          <option>EURUSD</option>
          <option>EUR/USD</option>
        </select>
        <button class="btn btn-fx btn-sm" (click)="refresh()">Refresh quote</button>
      </div>
    </div>

    @if (error()) {
      <div class="alert alert-warning">{{ error() }}</div>
    }

    <div class="row g-3 mb-3">
      <div class="col-md-4">
        <div class="fx-card fx-kpi h-100">
          <span class="label">Session regime</span>
          <span class="value" style="font-size: 1.25rem">{{ regime()?.sessionRegime || '—' }}</span>
          <span class="small" style="color: var(--fx-accent)">
            macro={{ regime()?.macroActive }} · last-look×{{ regime()?.lastLookPenaltyWeight }}
          </span>
        </div>
      </div>
      <div class="col-md-4">
        <div class="fx-card fx-kpi h-100">
          <span class="label">Effective markup (bps)</span>
          <span class="value" style="font-size: 1.25rem">{{ quote()?.allInSpreadBps ?? regime()?.sampleMarkupBps ?? '—' }}</span>
          <span class="small" style="color: var(--fx-accent)">tier {{ tier }}</span>
        </div>
      </div>
      <div class="col-md-4">
        <div class="fx-card fx-kpi h-100">
          <span class="label">Executable BUY</span>
          <span class="value" style="font-size: 1.25rem">{{ quote()?.executableRate ?? '—' }}</span>
          <span class="small" style="color: var(--fx-accent)">{{ quote()?.quoteId || 'request a quote' }}</span>
        </div>
      </div>
    </div>

    <div class="fx-card">
      <h2 class="h5 mb-3">Rates snapshot</h2>
      @if (rates()) {
        <div class="d-flex flex-wrap gap-4">
          <div><strong>Bid</strong> {{ rates()!.bid }}</div>
          <div><strong>Ask</strong> {{ rates()!.ask }}</div>
          <div><strong>Mid</strong> {{ rates()!.mid }}</div>
          <div><strong>LPs</strong> {{ rates()!.activeLpCount }}</div>
          <div><strong>Regime</strong> {{ rates()!.sessionRegime }}</div>
        </div>
      } @else {
        <p class="mb-0" style="color: var(--fx-ink-soft)">Click Refresh quote to pull live FR-01 pricing.</p>
      }
    </div>
  `,
})
export class BlotterPage implements OnInit {
  private readonly http = inject(HttpClient);
  readonly api = API;
  pair = 'EURUSD';
  tier = 'TIER_2';
  readonly quote = signal<Indicative | null>(null);
  readonly regime = signal<RegimeContext | null>(null);
  readonly rates = signal<{
    bid: number;
    ask: number;
    mid: number;
    activeLpCount: number;
    sessionRegime: string;
  } | null>(null);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.error.set(null);
    const pair = this.pair.replace('/', '');
    this.http
      .get<RegimeContext>(`${API.quote}/api/v1/fx/regime/context`, {
        params: { clientTier: this.tier, ccyPair: pair },
      })
      .subscribe({
        next: (r) => this.regime.set(r),
        error: (err) => this.error.set(`Regime context failed: ${err.message || err.status}`),
      });
    this.http.get(`${API.quote}/api/v1/fx/rates/${pair}`).subscribe({
      next: (r: any) => this.rates.set(r),
      error: (err) => this.error.set(`Rates failed: ${err.message || err.status}`),
    });
    this.http
      .post<Indicative>(`${API.quote}/api/v1/fx/quotes/indicative`, {
        clientId: 'CORP-100',
        clientTier: this.tier,
        ccyPair: pair,
        side: 'BUY',
        notional: 1000000,
        tenor: 'SPOT',
      })
      .subscribe({
        next: (q) => this.quote.set(q),
        error: (err) => this.error.set(`Indicative failed: ${err.message || err.status}`),
      });
  }
}
