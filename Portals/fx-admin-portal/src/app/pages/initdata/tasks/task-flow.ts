import { Component, input } from '@angular/core';
import { DatePipe } from '@angular/common';

export type TaskRow = {
  seq: number;
  taskId: string;
  name: string;
  type: string;
  status: string;
  inputJson?: string;
  outputJson?: string;
  errorDetails?: string;
  startedAt?: string;
  completedAt?: string;
  durationMs?: number;
};

/** Flow view for one run: each task as a step, then the per-task input, output, and error. */
@Component({
  selector: 'app-task-flow',
  imports: [DatePipe],
  template: `
    @if (tasks().length === 0) {
      <div class="small" style="color: var(--fx-ink-soft)">No run yet.</div>
    } @else {
      <div class="d-flex flex-wrap align-items-center gap-2 mb-3">
        @for (t of tasks(); track t.seq; let last = $last) {
          <span class="badge" [class]="badgeClass(t.status)">{{ t.name }}</span>
          @if (!last) { <i class="bi bi-arrow-right" style="color: var(--fx-ink-soft)"></i> }
        }
      </div>
      <div class="table-responsive">
        <table class="table table-sm align-middle mb-0">
          <thead>
            <tr><th>#</th><th>Task</th><th>Type</th><th>Status</th><th>Started</th><th>Duration</th><th>Input</th><th>Output / error</th></tr>
          </thead>
          <tbody>
            @for (t of tasks(); track t.seq) {
              <tr>
                <td>{{ t.seq + 1 }}</td>
                <td><code>{{ t.taskId }}</code><div class="small">{{ t.name }}</div></td>
                <td>{{ t.type }}</td>
                <td><span class="badge" [class]="badgeClass(t.status)">{{ t.status }}</span></td>
                <td class="small">{{ t.startedAt | date: 'short' }}</td>
                <td class="small">{{ t.durationMs ?? '—' }} ms</td>
                <td><pre class="small mb-0">{{ pretty(t.inputJson) }}</pre></td>
                <td>
                  @if (t.errorDetails) {
                    <pre class="small mb-0 text-danger">{{ t.errorDetails }}</pre>
                  } @else {
                    <pre class="small mb-0">{{ pretty(t.outputJson) }}</pre>
                  }
                </td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    }
  `,
})
export class TaskFlow {
  readonly tasks = input.required<TaskRow[]>();

  badgeClass(status: string): string {
    switch (status) {
      case 'COMPLETED': return 'text-bg-success';
      case 'FAILED': return 'text-bg-danger';
      case 'RUNNING': return 'text-bg-warning';
      default: return 'text-bg-secondary';
    }
  }

  pretty(json?: string): string {
    if (!json) {
      return '';
    }
    try {
      return JSON.stringify(JSON.parse(json), null, 2);
    } catch {
      return json;
    }
  }
}
