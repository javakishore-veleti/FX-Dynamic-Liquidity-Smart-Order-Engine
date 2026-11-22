import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { API } from '../api';

type ProcessTask = { taskId: string; name: string; type: string; status: string };
type ProcessView = { wfExecId: string; processKey: string; status: string; tasks: ProcessTask[] };

@Component({
  selector: 'app-convert',
  imports: [FormsModule],
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
      <div>
        <h1 class="h3 mb-1">All-In Rate Visualizer · FR-01</h1>
        <p class="mb-0" style="color: var(--fx-ink-soft)">
          Live retail-tier markup from quote {{ api.quote }} · convert requires start criteria
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

    <div class="row g-3">
      <div class="col-lg-5">
        <div class="fx-card">
          <label class="form-label fw-bold">You send (USD notional)</label>
          <input type="number" class="form-control form-control-lg mb-3" [(ngModel)]="amount" min="1" max="100000" />
          <label class="form-label fw-bold">Client tier</label>
          <select class="form-select mb-3" [(ngModel)]="tier" (ngModelChange)="refresh()">
            <option>TIER_2</option>
            <option>TIER_3</option>
            <option>TIER_1</option>
          </select>
          <label class="form-label fw-bold">Purpose</label>
          <input class="form-control mb-3" [(ngModel)]="reason" placeholder="e.g. Travel payment EUR" />
          <div class="form-check mb-3">
            <input class="form-check-input" type="checkbox" id="retAck" [(ngModel)]="ack" />
            <label class="form-check-label" for="retAck">I confirm amount and purpose for this convert</label>
          </div>
          <div class="fx-kpi mb-3">
            <span class="label">Executable EUR (indicative)</span>
            <span class="value">{{ quote()?.executableRate ?? '—' }}</span>
          </div>
          <button class="btn btn-fx-outline w-100 mb-2" (click)="refresh()">Refresh indicative</button>
          <button class="btn btn-coral w-100" [disabled]="!canStart()" (click)="runConvert()">
            Validate &amp; run convert
          </button>
          @if (!canStart()) {
            <div class="small mt-2" style="color: var(--fx-ink-soft)">{{ blockedReason() }}</div>
          }
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

    @if (processRun(); as proc) {
      <div class="fx-card mt-3">
        <h2 class="h5 mb-2">Convert steps</h2>
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
export class ConvertPage implements OnInit {
  private readonly http = inject(HttpClient);
  readonly api = API;
  amount = 2500;
  tier = 'TIER_3';
  reason = '';
  ack = false;
  readonly quote = signal<any | null>(null);
  readonly regime = signal<any | null>(null);
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
    if (!this.amount || this.amount <= 0) return 'Amount required';
    if (this.amount > 100_000) return 'Retail convert max 100,000';
    if (!this.reason || this.reason.trim().length < 8) return 'Purpose required';
    if (!this.ack) return 'Confirmation required';
    return null;
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

  runConvert(): void {
    const blocked = this.blockedReason();
    if (blocked) {
      this.error.set(blocked);
      return;
    }
    this.http
      .post<ProcessView>(`${API.admin}/api/v1/admin/bpmn/FR01_RETAIL_CONVERT/start`, {
        triggeredBy: 'RETAIL_PORTAL',
        clientTier: this.tier,
        ccyPair: 'EURUSD',
        clientId: 'RETAIL-1',
        notional: this.amount,
        reason: this.reason.trim(),
      })
      .subscribe({
        next: (view) => {
          this.processRun.set(view);
          this.banner.set(`Convert started · ${view.wfExecId}`);
          this.ack = false;
          this.refresh();
        },
        error: (err) => this.error.set(`Convert failed: ${err.message || err.status}`),
      });
  }
}
