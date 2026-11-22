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
                  <a class="btn btn-fx btn-sm" [routerLink]="['/workflows/executions']" [queryParams]="{ start: row.workflowKey }">
                    Run / view executions
                  </a>
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
