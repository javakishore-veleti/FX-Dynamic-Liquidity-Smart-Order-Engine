import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { API } from '../../api';
import { TaskFlow, TaskRow } from './tasks/task-flow';

type Dataset = {
  datasetKey: string;
  title: string;
  description: string;
  ownerService: string;
  workflowKey: string;
  setupStatus: string;
  setupAllowed: boolean;
  wfExecId?: string;
  startedAt?: string;
  completedAt?: string;
  tasks: TaskRow[];
};

const DROPDOWN_ABOVE = 5;
const MIN_REASON = 8;

/** Initial Data: each dataset is loaded once by its Setup button. Nothing else loads data. */
@Component({
  selector: 'app-initdata',
  imports: [FormsModule, DatePipe, TaskFlow],
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
      <div>
        <h1 class="h3 mb-1">Initial Data</h1>
        <p class="mb-0" style="color: var(--fx-ink-soft)">
          Load reference data once. Each Setup runs a workflow; its tasks are shown below the dataset.
        </p>
      </div>
      <button class="btn btn-fx-outline btn-sm" (click)="reload()">Refresh</button>
    </div>

    @if (error()) { <div class="alert alert-warning">{{ error() }}</div> }
    @if (banner()) { <div class="alert alert-success">{{ banner() }}</div> }

    @if (showDropdown()) {
      <div class="mb-3" style="max-width: 420px">
        <label class="form-label fw-bold">Dataset</label>
        <select class="form-select" [ngModel]="selectedKey()" (ngModelChange)="selectedKey.set($event)">
          @for (d of datasets(); track d.datasetKey) {
            <option [value]="d.datasetKey">{{ d.title }} — {{ statusLabel(d) }}</option>
          }
        </select>
      </div>
    }

    @for (d of visible(); track d.datasetKey) {
      <details class="fx-card mb-3" open>
        <summary class="d-flex flex-wrap justify-content-between align-items-center gap-2" style="cursor: pointer">
          <span>
            <strong>{{ d.title }}</strong>
            <span class="small ms-2" style="color: var(--fx-ink-soft)">owner: {{ d.ownerService }}</span>
          </span>
          <span class="badge" [class]="badgeClass(d.setupStatus)">{{ statusLabel(d) }}</span>
        </summary>

        <div class="mt-3">
          <p class="mb-3">{{ d.description }}</p>

          @if (d.setupAllowed) {
            <div class="row g-2 align-items-end mb-3">
              <div class="col-md-8">
                <label class="form-label fw-bold">Business reason (min {{ minReason }} chars)</label>
                <input class="form-control" [ngModel]="reasonFor(d.datasetKey)"
                       (ngModelChange)="setReason(d.datasetKey, $event)" />
              </div>
              <div class="col-md-4">
                <button class="btn btn-fx btn-sm w-100"
                        [disabled]="busy() || reasonFor(d.datasetKey).trim().length < minReason"
                        (click)="setup(d)">
                  {{ busy() ? 'Setting up…' : 'Setup ' + d.title }}
                </button>
              </div>
            </div>
          } @else if (d.setupStatus === 'COMPLETED') {
            <div class="alert alert-success mb-3 py-2">
              Set up on {{ d.completedAt | date: 'medium' }}. This dataset is loaded once.
            </div>
          }

          <app-task-flow [tasks]="d.tasks" />
        </div>
      </details>
    }
  `,
})
export class InitDataPage implements OnInit {
  readonly minReason = MIN_REASON;

  private readonly http = inject(HttpClient);

  readonly datasets = signal<Dataset[]>([]);
  readonly selectedKey = signal<string>('');
  readonly error = signal('');
  readonly banner = signal('');
  readonly busy = signal(false);
  private readonly reasons = signal<Record<string, string>>({});

  readonly showDropdown = computed(() => this.datasets().length > DROPDOWN_ABOVE);
  readonly visible = computed(() => {
    const all = this.datasets();
    if (!this.showDropdown()) {
      return all;
    }
    return all.filter((d) => d.datasetKey === this.selectedKey());
  });

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.http.get<Dataset[]>(`${API.admin}/api/v1/admin/initial-data`).subscribe({
      next: (rows) => {
        this.datasets.set(rows);
        if (!rows.some((d) => d.datasetKey === this.selectedKey()) && rows.length) {
          this.selectedKey.set(rows[0].datasetKey);
        }
        this.error.set('');
      },
      error: (e) => this.error.set(this.messageOf(e, 'Could not load Initial Data.')),
    });
  }

  reasonFor(key: string): string {
    return this.reasons()[key] ?? '';
  }

  setReason(key: string, value: string): void {
    this.reasons.update((r) => ({ ...r, [key]: value }));
  }

  setup(d: Dataset): void {
    const reason = this.reasonFor(d.datasetKey).trim();
    if (reason.length < MIN_REASON) {
      this.error.set(`Business reason is required (min ${MIN_REASON} chars).`);
      return;
    }
    this.busy.set(true);
    this.error.set('');
    this.banner.set('');
    const body = { reason, triggeredBy: 'ADMIN_PORTAL' };
    this.http
      .post<Dataset>(`${API.admin}/api/v1/admin/initial-data/${d.datasetKey}/setup`, body)
      .subscribe({
        next: (updated) => {
          this.datasets.update((rows) => rows.map((r) => (r.datasetKey === updated.datasetKey ? updated : r)));
          this.busy.set(false);
          this.banner.set(
            updated.setupStatus === 'COMPLETED'
              ? `${updated.title} is set up.`
              : `${updated.title} setup ended with status ${updated.setupStatus}. See the task flow.`,
          );
        },
        error: (e) => {
          this.busy.set(false);
          this.error.set(this.messageOf(e, 'Setup failed.'));
          this.reload();
        },
      });
  }

  statusLabel(d: Dataset): string {
    switch (d.setupStatus) {
      case 'COMPLETED': return 'Set up';
      case 'RUNNING': return 'Running';
      case 'FAILED': return 'Failed — can retry';
      default: return 'Not set up';
    }
  }

  badgeClass(status: string): string {
    switch (status) {
      case 'COMPLETED': return 'text-bg-success';
      case 'FAILED': return 'text-bg-danger';
      case 'RUNNING': return 'text-bg-warning';
      default: return 'text-bg-secondary';
    }
  }

  private messageOf(e: unknown, fallback: string): string {
    const err = e as { error?: { message?: string }; message?: string };
    return err?.error?.message || err?.message || fallback;
  }
}
