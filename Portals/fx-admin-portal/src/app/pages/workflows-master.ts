import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { API } from '../api';

type WorkflowDef = {
  workflowKey: string;
  name: string;
  description: string;
  bpmnResource: string;
  version: string;
  portalScope: string;
  status: string;
  frId: string;
};

@Component({
  selector: 'app-workflows-master',
  imports: [RouterLink],
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
      <div>
        <h1 class="h3 mb-1">Workflows · Master List</h1>
        <p class="mb-0" style="color: var(--fx-ink-soft)">
          Process catalog for session guardrails, quote→route, and retail convert · {{ api.admin }}
        </p>
      </div>
      <button class="btn btn-fx-outline btn-sm" (click)="reload()">Refresh</button>
    </div>

    @if (error()) {
      <div class="alert alert-warning">{{ error() }}</div>
    }

    <div class="fx-card">
      <div class="table-responsive">
        <table class="table align-middle mb-0">
          <thead>
            <tr>
              <th>Process key</th>
              <th>Functional name</th>
              <th>Requirement</th>
              <th>Audience</th>
              <th>Definition</th>
              <th>Status</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            @for (row of rows(); track row.workflowKey) {
              <tr>
                <td><code>{{ row.workflowKey }}</code></td>
                <td class="fw-semibold">{{ row.name }}</td>
                <td><span class="badge badge-fx">{{ row.frId || '—' }}</span></td>
                <td>{{ audienceLabel(row.portalScope) }}</td>
                <td class="small">{{ row.bpmnResource }}</td>
                <td><span class="badge badge-fx">{{ row.status }}</span></td>
                <td class="text-end">
                  @if (isSeed(row.workflowKey)) {
                    <button class="btn btn-fx btn-sm me-2"
                            [disabled]="seedBusy() === row.workflowKey"
                            (click)="runSeed(row.workflowKey)">
                      {{ seedBusy() === row.workflowKey ? 'Running…' : 'Run setup' }}
                    </button>
                  }
                  <a class="btn btn-fx btn-sm" [routerLink]="['/workflows/executions']" [queryParams]="{ start: row.workflowKey }">
                    Run / view executions
                  </a>
                  @if (seedMessage()[row.workflowKey]; as msg) {
                    <div class="small mt-1" [class.text-success]="msg.ok" [class.text-danger]="!msg.ok">{{ msg.text }}</div>
                  }
                </td>
              </tr>
            } @empty {
              <tr>
                <td colspan="7" style="color: var(--fx-ink-soft)">No workflow definitions loaded.</td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    </div>
  `,
})
export class WorkflowsMasterPage implements OnInit {
  private readonly http = inject(HttpClient);
  readonly api = API;
  readonly rows = signal<WorkflowDef[]>([]);
  readonly seedBusy = signal<string | null>(null);
  readonly seedMessage = signal<Record<string, { ok: boolean; text: string }>>({});

  /** Seed workflows are the INIT_* rows. Each runs once from this button and takes no typed input. */
  isSeed(workflowKey: string): boolean {
    return workflowKey.startsWith('INIT_');
  }

  runSeed(workflowKey: string): void {
    const dataset = workflowKey.replace(/^INIT_/, '');
    this.seedBusy.set(workflowKey);
    this.http
      .post<{ setupStatus: string; title: string }>(
        `${API.admin}/api/v1/admin/initial-data/${dataset}/setup`,
        { triggeredBy: 'ADMIN_PORTAL' },
      )
      .subscribe({
        next: (r) => {
          this.seedBusy.set(null);
          this.setMsg(workflowKey, r.setupStatus === 'COMPLETED', `${r.title}: ${r.setupStatus}`);
        },
        error: (e) => {
          this.seedBusy.set(null);
          this.setMsg(workflowKey, false, e?.error?.message || e?.message || 'Setup failed');
        },
      });
  }

  private setMsg(key: string, ok: boolean, text: string): void {
    this.seedMessage.update((m) => ({ ...m, [key]: { ok, text } }));
  }
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.reload();
  }

  audienceLabel(scope: string): string {
    switch ((scope || '').toUpperCase()) {
      case 'ADMIN':
        return 'Dealing desk / Admin';
      case 'CORPORATE':
        return 'Corporate client';
      case 'RETAIL':
        return 'Retail client';
      default:
        return scope || '—';
    }
  }

  reload(): void {
    this.error.set(null);
    this.http.get<WorkflowDef[]>(`${API.admin}/api/v1/admin/workflow-definitions`).subscribe({
      next: (data) => this.rows.set(data ?? []),
      error: (err) => this.error.set(`Load failed: ${err.message || err.status}`),
    });
  }
}
