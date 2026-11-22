import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { API } from '../api';

type ExecRow = {
  wfExecId: string;
  workflowType: string;
  workflowKey?: string;
  status: string;
  startedAt: string;
  completedAt?: string;
  batchesCompleted: number;
  triggeredBy?: string;
  executionSummary?: string;
  errorDetails?: string;
};

type ProcessTask = {
  taskId: string;
  name: string;
  type: string;
  status: string;
  detail?: string;
  startedAt?: string;
  completedAt?: string;
};

type ProcessView = {
  wfExecId: string;
  processKey: string;
  status: string;
  currentTaskId?: string;
  currentTaskName?: string;
  tasks: ProcessTask[];
  variables?: Record<string, unknown>;
};

const PROCESS_OPTIONS: { key: string; label: string; audience: string }[] = [
  {
    key: 'FR01_SESSION_REGIME_GUARDRAILS',
    label: 'Session & regime guardrails',
    audience: 'Dealing desk',
  },
  {
    key: 'FR01_CORPORATE_QUOTE_ROUTE',
    label: 'Corporate quote → route',
    audience: 'Corporate',
  },
  {
    key: 'FR01_RETAIL_CONVERT',
    label: 'Retail convert',
    audience: 'Retail',
  },
];

@Component({
  selector: 'app-workflows-executions',
  imports: [FormsModule, DatePipe],
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
      <div>
        <h1 class="h3 mb-1">Workflows · Run history</h1>
        <p class="mb-0" style="color: var(--fx-ink-soft)">
          Criteria-gated process starts · full step trail for audit and desk review
        </p>
      </div>
      <button class="btn btn-fx-outline btn-sm" (click)="reload()">Refresh history</button>
    </div>

    @if (error()) {
      <div class="alert alert-warning">{{ error() }}</div>
    }
    @if (banner()) {
      <div class="alert alert-success">{{ banner() }}</div>
    }

    <div class="fx-card mb-3">
      <h2 class="h5 mb-3">Start criteria</h2>
      <div class="row g-3">
        <div class="col-md-4">
          <label class="form-label fw-bold">Process</label>
          <select class="form-select" [(ngModel)]="criteria.workflowKey" (ngModelChange)="onProcessChange()">
            @for (opt of processOptions; track opt.key) {
              <option [value]="opt.key">{{ opt.label }}</option>
            }
          </select>
          <div class="form-text">Audience: {{ currentAudience() }}</div>
        </div>
        <div class="col-md-2">
          <label class="form-label fw-bold">Client tier</label>
          <select class="form-select" [(ngModel)]="criteria.clientTier">
            <option>TIER_1</option>
            <option>TIER_2</option>
            <option>TIER_3</option>
          </select>
        </div>
        <div class="col-md-2">
          <label class="form-label fw-bold">CCY pair</label>
          <select class="form-select" [(ngModel)]="criteria.ccyPair">
            <option>EURUSD</option>
            <option>USDJPY</option>
            <option>GBPUSD</option>
          </select>
        </div>
        <div class="col-md-2">
          <label class="form-label fw-bold">Notional (USD)</label>
          <input type="number" class="form-control" [(ngModel)]="criteria.notional" min="1" />
        </div>
        <div class="col-md-2">
          <label class="form-label fw-bold">Client id</label>
          <input class="form-control" [(ngModel)]="criteria.clientId" />
        </div>
        <div class="col-md-4">
          <label class="form-label fw-bold">Business reason</label>
          <input
            class="form-control"
            [(ngModel)]="criteria.reason"
            placeholder="e.g. FOMC window desk review / client RFQ block"
          />
        </div>
        <div class="col-md-4">
          <label class="form-label fw-bold">Triggered by</label>
          <input class="form-control" [(ngModel)]="criteria.triggeredBy" />
        </div>
        <div class="col-md-4 d-flex align-items-end">
          <div class="form-check">
            <input class="form-check-input" type="checkbox" id="ackRisk" [(ngModel)]="criteria.ackRisk" />
            <label class="form-check-label" for="ackRisk">
              I confirm criteria are correct and this run is authorized
            </label>
          </div>
        </div>
        @if (criteria.workflowKey === 'FR01_SESSION_REGIME_GUARDRAILS') {
          <div class="col-12">
            <div class="form-check">
              <input
                class="form-check-input"
                type="checkbox"
                id="preConfirmMacro"
                [(ngModel)]="criteria.confirmMacro"
              />
              <label class="form-check-label" for="preConfirmMacro">
                Pre-authorize MACRO_NEWS_HIGH arm if calendar window is active (still requires desk confirm step when
                waiting)
              </label>
            </div>
          </div>
        }
        <div class="col-12">
          <button class="btn btn-fx" [disabled]="!canStart()" (click)="startProcess()">
            Validate &amp; start process
          </button>
          @if (!canStart()) {
            <span class="ms-3 small" style="color: var(--fx-ink-soft)">{{ startBlockedReason() }}</span>
          }
        </div>
      </div>
    </div>

    <div class="row g-3">
      <div class="col-lg-6">
        <div class="fx-card h-100">
          <h2 class="h5 mb-3">Run history</h2>
          <div class="table-responsive" style="max-height: 28rem; overflow: auto">
            <table class="table align-middle mb-0 table-sm">
              <thead>
                <tr>
                  <th>Started</th>
                  <th>Process</th>
                  <th>Status</th>
                  <th>By</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                @for (row of rows(); track row.wfExecId) {
                  <tr [class.table-active]="selected()?.wfExecId === row.wfExecId">
                    <td class="small">{{ row.startedAt | date: 'short' }}</td>
                    <td class="small">{{ processLabel(row.workflowKey) }}</td>
                    <td><span class="badge badge-fx">{{ row.status }}</span></td>
                    <td class="small">{{ row.triggeredBy || '—' }}</td>
                    <td class="text-end">
                      <button class="btn btn-fx-outline btn-sm" (click)="open(row.wfExecId)">Open steps</button>
                    </td>
                  </tr>
                } @empty {
                  <tr>
                    <td colspan="5" style="color: var(--fx-ink-soft)">No runs yet.</td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div class="col-lg-6">
        <div class="fx-card h-100">
          <h2 class="h5 mb-3">Task steps</h2>
          @if (selected(); as proc) {
            <div class="mb-3 small">
              <div><strong>Run id</strong> <code>{{ proc.wfExecId }}</code></div>
              <div>
                <strong>Process</strong> {{ processLabel(proc.processKey) }} ·
                <strong>status</strong> {{ proc.status }}
              </div>
              @if (proc.currentTaskName) {
                <div><strong>Waiting on</strong> {{ proc.currentTaskName }}</div>
              }
            </div>
            <ol class="list-group list-group-numbered mb-3">
              @for (t of proc.tasks; track t.taskId) {
                <li class="list-group-item d-flex justify-content-between align-items-start">
                  <div class="ms-2 me-auto">
                    <div class="fw-semibold">{{ t.name }}</div>
                    <div class="small" style="color: var(--fx-ink-soft)">{{ taskKindLabel(t.type) }}</div>
                    @if (t.detail) {
                      <div class="small mt-1" style="word-break: break-all">{{ t.detail }}</div>
                    }
                  </div>
                  <span class="badge badge-fx">{{ t.status }}</span>
                </li>
              }
            </ol>
            @if (proc.status === 'RUNNING' && proc.currentTaskId === 'task_confirm_macro') {
              <button class="btn btn-coral btn-sm" (click)="confirmMacro(proc.wfExecId)">
                Confirm arm MACRO_NEWS_HIGH
              </button>
            }
          } @else {
            <p class="mb-0" style="color: var(--fx-ink-soft)">
              Complete start criteria and launch a process, or open a run from history.
            </p>
          }
        </div>
      </div>
    </div>
  `,
})
export class WorkflowsExecutionsPage implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly route = inject(ActivatedRoute);
  readonly api = API;
  readonly processOptions = PROCESS_OPTIONS;

  criteria = {
    workflowKey: 'FR01_SESSION_REGIME_GUARDRAILS',
    clientTier: 'TIER_2',
    ccyPair: 'EURUSD',
    notional: 2_500_000,
    clientId: 'CORP-100',
    reason: '',
    triggeredBy: 'DESK_OPERATOR',
    ackRisk: false,
    confirmMacro: false,
  };

  readonly rows = signal<ExecRow[]>([]);
  readonly selected = signal<ProcessView | null>(null);
  readonly error = signal<string | null>(null);
  readonly banner = signal<string | null>(null);

  ngOnInit(): void {
    const start = this.route.snapshot.queryParamMap.get('start');
    if (start) {
      this.criteria.workflowKey = start;
      this.onProcessChange();
    }
    this.reload();
  }

  onProcessChange(): void {
    if (this.criteria.workflowKey === 'FR01_RETAIL_CONVERT') {
      this.criteria.clientId = 'RETAIL-1';
      this.criteria.clientTier = 'TIER_3';
      this.criteria.notional = 2_500;
    } else if (this.criteria.workflowKey === 'FR01_CORPORATE_QUOTE_ROUTE') {
      this.criteria.clientId = 'CORP-100';
      this.criteria.clientTier = 'TIER_2';
      this.criteria.notional = 2_500_000;
    } else {
      this.criteria.clientId = 'CORP-100';
      this.criteria.clientTier = 'TIER_2';
      this.criteria.notional = 2_500_000;
    }
  }

  currentAudience(): string {
    return PROCESS_OPTIONS.find((o) => o.key === this.criteria.workflowKey)?.audience || '—';
  }

  canStart(): boolean {
    return this.startBlockedReason() === null;
  }

  startBlockedReason(): string | null {
    if (!this.criteria.workflowKey) {
      return 'Select a process';
    }
    if (!this.criteria.ccyPair?.trim()) {
      return 'CCY pair required';
    }
    if (!this.criteria.clientId?.trim()) {
      return 'Client id required';
    }
    if (!this.criteria.notional || this.criteria.notional <= 0) {
      return 'Notional must be > 0';
    }
    if (this.criteria.workflowKey === 'FR01_RETAIL_CONVERT' && this.criteria.notional > 100_000) {
      return 'Retail convert notional must be ≤ 100,000';
    }
    if (
      (this.criteria.workflowKey === 'FR01_CORPORATE_QUOTE_ROUTE' ||
        this.criteria.workflowKey === 'FR01_SESSION_REGIME_GUARDRAILS') &&
      this.criteria.notional < 100_000
    ) {
      return 'Institutional notional must be ≥ 100,000';
    }
    if (!this.criteria.reason || this.criteria.reason.trim().length < 12) {
      return 'Business reason required (min 12 chars)';
    }
    if (!this.criteria.triggeredBy?.trim()) {
      return 'Triggered by required';
    }
    if (!this.criteria.ackRisk) {
      return 'Authorization checkbox required';
    }
    return null;
  }

  processLabel(key?: string | null): string {
    const hit = PROCESS_OPTIONS.find((o) => o.key === key);
    return hit?.label || key || '—';
  }

  taskKindLabel(type: string): string {
    switch ((type || '').toUpperCase()) {
      case 'USER':
        return 'Desk action';
      case 'GATEWAY':
        return 'Decision';
      case 'SERVICE':
        return 'System step';
      default:
        return type || 'Step';
    }
  }

  reload(): void {
    this.error.set(null);
    this.http
      .get<{ content: ExecRow[] }>(`${API.admin}/api/v1/admin/workflows`, {
        params: { size: 50, page: 0 },
      })
      .subscribe({
        next: (page) => this.rows.set(page?.content ?? []),
        error: (err) => this.error.set(`Load history failed: ${err.message || err.status}`),
      });
  }

  startProcess(): void {
    const blocked = this.startBlockedReason();
    if (blocked) {
      this.error.set(blocked);
      return;
    }
    this.error.set(null);
    const body: Record<string, unknown> = {
      triggeredBy: this.criteria.triggeredBy.trim(),
      clientTier: this.criteria.clientTier,
      ccyPair: this.criteria.ccyPair,
      clientId: this.criteria.clientId.trim(),
      notional: this.criteria.notional,
      reason: this.criteria.reason.trim(),
      confirmMacro: this.criteria.confirmMacro,
    };
    this.http
      .post<ProcessView>(`${API.admin}/api/v1/admin/bpmn/${this.criteria.workflowKey}/start`, body)
      .subscribe({
        next: (view) => {
          this.selected.set(view);
          this.banner.set(`Started ${this.processLabel(view.processKey)} · run ${view.wfExecId}`);
          this.criteria.ackRisk = false;
          this.reload();
        },
        error: (err) => this.error.set(`Start failed: ${err.message || err.status}`),
      });
  }

  open(wfExecId: string): void {
    this.error.set(null);
    this.http.get<ProcessView>(`${API.admin}/api/v1/admin/bpmn/executions/${wfExecId}`).subscribe({
      next: (view) => this.selected.set(view),
      error: () => {
        const row = this.rows().find((r) => r.wfExecId === wfExecId);
        if (!row) {
          this.error.set('Run not found');
          return;
        }
        this.selected.set({
          wfExecId: row.wfExecId,
          processKey: row.workflowKey || row.workflowType,
          status: row.status,
          tasks: [
            {
              taskId: 'ops',
              name: row.workflowType,
              type: 'SERVICE',
              status: row.status,
              detail: row.executionSummary || row.errorDetails || '',
            },
          ],
        });
      },
    });
  }

  confirmMacro(wfExecId: string): void {
    this.http
      .post<ProcessView>(`${API.admin}/api/v1/admin/bpmn/executions/${wfExecId}/complete-task`, {
        confirmMacro: true,
      })
      .subscribe({
        next: (view) => {
          this.selected.set(view);
          this.banner.set('MACRO_NEWS_HIGH confirmed · process continued');
          this.reload();
        },
        error: (err) => this.error.set(`Confirm failed: ${err.message || err.status}`),
      });
  }
}
