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

type BpmnTask = {
  taskId: string;
  name: string;
  type: string;
  status: string;
  detail?: string;
  startedAt?: string;
  completedAt?: string;
};

type BpmnView = {
  wfExecId: string;
  processKey: string;
  status: string;
  currentTaskId?: string;
  currentTaskName?: string;
  tasks: BpmnTask[];
  variables?: Record<string, unknown>;
};

@Component({
  selector: 'app-workflows-executions',
  imports: [FormsModule, DatePipe],
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
      <div>
        <h1 class="h3 mb-1">Workflows · Executions</h1>
        <p class="mb-0" style="color: var(--fx-ink-soft)">
          Every BPMN start is a <code>wf_exec_log</code> row · task view below
        </p>
      </div>
      <div class="d-flex flex-wrap gap-2 align-items-center">
        <select class="form-select form-select-sm" style="width: auto" [(ngModel)]="startKey">
          <option value="FR01_SESSION_REGIME_GUARDRAILS">FR01_SESSION_REGIME_GUARDRAILS</option>
          <option value="FR01_CORPORATE_QUOTE_ROUTE">FR01_CORPORATE_QUOTE_ROUTE</option>
          <option value="FR01_RETAIL_CONVERT">FR01_RETAIL_CONVERT</option>
        </select>
        <button class="btn btn-fx btn-sm" (click)="startBpmn()">Start BPMN</button>
        <button class="btn btn-fx-outline btn-sm" (click)="reload()">Refresh logs</button>
      </div>
    </div>

    @if (error()) {
      <div class="alert alert-warning">{{ error() }}</div>
    }
    @if (banner()) {
      <div class="alert alert-success">{{ banner() }}</div>
    }

    <div class="row g-3">
      <div class="col-lg-6">
        <div class="fx-card h-100">
          <h2 class="h5 mb-3">wf_exec_log</h2>
          <div class="table-responsive" style="max-height: 28rem; overflow: auto">
            <table class="table align-middle mb-0 table-sm">
              <thead>
                <tr>
                  <th>Started</th>
                  <th>Key</th>
                  <th>Type</th>
                  <th>Status</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                @for (row of rows(); track row.wfExecId) {
                  <tr [class.table-active]="selected()?.wfExecId === row.wfExecId">
                    <td class="small">{{ row.startedAt | date: 'short' }}</td>
                    <td class="small"><code>{{ row.workflowKey || '—' }}</code></td>
                    <td class="small">{{ row.workflowType }}</td>
                    <td><span class="badge badge-fx">{{ row.status }}</span></td>
                    <td class="text-end">
                      <button class="btn btn-fx-outline btn-sm" (click)="open(row.wfExecId)">Tasks</button>
                    </td>
                  </tr>
                } @empty {
                  <tr>
                    <td colspan="5" style="color: var(--fx-ink-soft)">No executions yet.</td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div class="col-lg-6">
        <div class="fx-card h-100">
          <h2 class="h5 mb-3">BPMN task execution</h2>
          @if (selected(); as proc) {
            <div class="mb-3 small">
              <div><strong>wfExecId</strong> <code>{{ proc.wfExecId }}</code></div>
              <div><strong>process</strong> {{ proc.processKey }} · <strong>status</strong> {{ proc.status }}</div>
              @if (proc.currentTaskName) {
                <div><strong>waiting on</strong> {{ proc.currentTaskName }}</div>
              }
            </div>
            <ol class="list-group list-group-numbered mb-3">
              @for (t of proc.tasks; track t.taskId) {
                <li class="list-group-item d-flex justify-content-between align-items-start">
                  <div class="ms-2 me-auto">
                    <div class="fw-semibold">{{ t.name }}</div>
                    <div class="small" style="color: var(--fx-ink-soft)">{{ t.taskId }} · {{ t.type }}</div>
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
                Complete desk confirm (confirmMacro=true)
              </button>
            }
          } @else {
            <p class="mb-0" style="color: var(--fx-ink-soft)">Select an execution or start a BPMN run.</p>
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
  startKey = 'FR01_SESSION_REGIME_GUARDRAILS';
  readonly rows = signal<ExecRow[]>([]);
  readonly selected = signal<BpmnView | null>(null);
  readonly error = signal<string | null>(null);
  readonly banner = signal<string | null>(null);

  ngOnInit(): void {
    const start = this.route.snapshot.queryParamMap.get('start');
    if (start) {
      this.startKey = start;
    }
    this.reload();
    if (start) {
      this.startBpmn();
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
        error: (err) => this.error.set(`Load executions failed: ${err.message || err.status}`),
      });
  }

  startBpmn(): void {
    this.error.set(null);
    this.http
      .post<BpmnView>(`${API.admin}/api/v1/admin/bpmn/${this.startKey}/start`, {
        triggeredBy: 'ADMIN_PORTAL',
        clientTier: 'TIER_2',
        ccyPair: 'EURUSD',
        notional: 2500000,
      })
      .subscribe({
        next: (view) => {
          this.selected.set(view);
          this.banner.set(`Started ${view.processKey} · wfExecId=${view.wfExecId}`);
          this.reload();
        },
        error: (err) => this.error.set(`Start BPMN failed: ${err.message || err.status}`),
      });
  }

  open(wfExecId: string): void {
    this.error.set(null);
    this.http.get<BpmnView>(`${API.admin}/api/v1/admin/bpmn/executions/${wfExecId}`).subscribe({
      next: (view) => this.selected.set(view),
      error: () => {
        // Non-BPMN ops logs still appear in the list; show a minimal view from list row
        const row = this.rows().find((r) => r.wfExecId === wfExecId);
        if (!row) {
          this.error.set('Execution not found');
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
      .post<BpmnView>(`${API.admin}/api/v1/admin/bpmn/executions/${wfExecId}/complete-task`, {
        confirmMacro: true,
      })
      .subscribe({
        next: (view) => {
          this.selected.set(view);
          this.banner.set('Desk confirmed macro · BPMN continued');
          this.reload();
        },
        error: (err) => this.error.set(`Complete task failed: ${err.message || err.status}`),
      });
  }
}
