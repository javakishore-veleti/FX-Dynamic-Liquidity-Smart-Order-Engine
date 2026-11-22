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

type ProcessTask = { taskId: string; name: string; type: string; status: string; detail?: string };
type ProcessView = {
  wfExecId: string;
  processKey: string;
  status: string;
  tasks: ProcessTask[];
};

@Component({
  selector: 'app-blotter',
  imports: [FormsModule],
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
      <div>
        <h1 class="h3 mb-1">Multi-LP Depth Blotter · FR-01</h1>
        <p class="mb-0" style="color: var(--fx-ink-soft)">
          Live indicative from quote {{ api.quote }} · quote→route with start criteria
        </p>
      </div>
      <button class="btn btn-fx-outline btn-sm" (click)="refresh()">Refresh quote</button>
    </div>

    @if (error()) {
      <div class="alert alert-warning">{{ error() }}</div>
    }
    @if (banner()) {
      <div class="alert alert-success">{{ banner() }}</div>
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

    <div class="fx-card mb-3">
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

    <div class="fx-card mb-3">
      <h2 class="h5 mb-3">Quote → route · start criteria</h2>
      <div class="row g-3">
        <div class="col-md-3">
          <label class="form-label fw-bold">Client tier</label>
          <select class="form-select" [(ngModel)]="tier" (ngModelChange)="refresh()">
            <option>TIER_1</option>
            <option>TIER_2</option>
            <option>TIER_3</option>
          </select>
        </div>
        <div class="col-md-3">
          <label class="form-label fw-bold">CCY pair</label>
          <select class="form-select" [(ngModel)]="pair" (ngModelChange)="refresh()">
            <option>EURUSD</option>
            <option>USDJPY</option>
          </select>
        </div>
        <div class="col-md-3">
          <label class="form-label fw-bold">Notional (USD)</label>
          <input type="number" class="form-control" [(ngModel)]="notional" min="100000" />
        </div>
        <div class="col-md-3">
          <label class="form-label fw-bold">Client id</label>
          <input class="form-control" [(ngModel)]="clientId" />
        </div>
        <div class="col-md-8">
          <label class="form-label fw-bold">Business reason</label>
          <input class="form-control" [(ngModel)]="reason" placeholder="e.g. Block RFQ · London overlap sweep" />
        </div>
        <div class="col-md-4 d-flex align-items-end">
          <div class="form-check">
            <input class="form-check-input" type="checkbox" id="corpAck" [(ngModel)]="ack" />
            <label class="form-check-label" for="corpAck">Authorize quote→route for these criteria</label>
          </div>
        </div>
        <div class="col-12">
          <button class="btn btn-fx" [disabled]="!canStart()" (click)="runQuoteRoute()">
            Validate &amp; run quote → route
          </button>
          @if (!canStart()) {
            <span class="ms-3 small" style="color: var(--fx-ink-soft)">{{ blockedReason() }}</span>
          }
        </div>
      </div>
    </div>

    @if (processRun(); as proc) {
      <div class="fx-card">
        <h2 class="h5 mb-2">Quote → route steps</h2>
        <p class="small mb-3">Run <code>{{ proc.wfExecId }}</code> · {{ proc.status }}</p>
        <ol class="list-group list-group-numbered">
          @for (t of proc.tasks; track t.taskId) {
            <li class="list-group-item d-flex justify-content-between">
              <span>{{ t.name }}</span>
              <span class="badge badge-fx">{{ t.status }}</span>
            </li>
          }
        </ol>
      </div>
    }
  `,
})
export class BlotterPage implements OnInit {
  private readonly http = inject(HttpClient);
  readonly api = API;
  pair = 'EURUSD';
  tier = 'TIER_2';
  notional = 2_500_000;
  clientId = 'CORP-100';
  reason = '';
  ack = false;
  readonly quote = signal<Indicative | null>(null);
  readonly regime = signal<RegimeContext | null>(null);
  readonly rates = signal<{
    bid: number;
    ask: number;
    mid: number;
    activeLpCount: number;
    sessionRegime: string;
  } | null>(null);
  readonly processRun = signal<ProcessView | null>(null);
  readonly error = signal<string | null>(null);
  readonly banner = signal<string | null>(null);

  ngOnInit(): void {
    this.refresh();
  }

  canStart(): boolean {
    return this.blockedReason() === null;
  }

  blockedReason(): string | null {
    if (!this.clientId.trim()) return 'Client id required';
    if (!this.notional || this.notional < 100_000) return 'Notional must be ≥ 100,000';
    if (!this.reason || this.reason.trim().length < 12) return 'Business reason required (min 12 chars)';
    if (!this.ack) return 'Authorization required';
    return null;
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
        clientId: this.clientId,
        clientTier: this.tier,
        ccyPair: pair,
        side: 'BUY',
        notional: this.notional,
        tenor: 'SPOT',
      })
      .subscribe({
        next: (q) => this.quote.set(q),
        error: (err) => this.error.set(`Indicative failed: ${err.message || err.status}`),
      });
  }

  runQuoteRoute(): void {
    const blocked = this.blockedReason();
    if (blocked) {
      this.error.set(blocked);
      return;
    }
    this.error.set(null);
    this.http
      .post<ProcessView>(`${API.admin}/api/v1/admin/bpmn/FR01_CORPORATE_QUOTE_ROUTE/start`, {
        triggeredBy: 'CORPORATE_PORTAL',
        clientTier: this.tier,
        ccyPair: this.pair.replace('/', ''),
        clientId: this.clientId.trim(),
        notional: this.notional,
        reason: this.reason.trim(),
      })
      .subscribe({
        next: (view) => {
          this.processRun.set(view);
          this.banner.set(`Quote → route started · ${view.wfExecId}`);
          this.ack = false;
          this.refresh();
        },
        error: (err) => this.error.set(`Quote → route failed: ${err.message || err.status}`),
      });
  }
}
