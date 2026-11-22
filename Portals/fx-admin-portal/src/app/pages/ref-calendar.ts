import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { API } from '../api';

type CalRow = {
  source: string;
  severity: number;
  start: string;
  end: string;
  label: string;
  activeNow?: boolean;
};

@Component({
  selector: 'app-ref-calendar',
  imports: [FormsModule],
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
      <div>
        <h1 class="h3 mb-1">Reference · Economic Calendar</h1>
        <p class="mb-0" style="color: var(--fx-ink-soft)">
          Published FOMC/NFP/ECB windows · quote classpath seed + runtime overlay
        </p>
      </div>
      <button class="btn btn-fx-outline btn-sm" (click)="reload()">Refresh</button>
    </div>
    @if (error()) { <div class="alert alert-warning">{{ error() }}</div> }
    @if (banner()) { <div class="alert alert-success">{{ banner() }}</div> }
    <div class="row g-3">
      <div class="col-lg-4">
        <div class="fx-card">
          <h2 class="h5 mb-3">Upsert window</h2>
          <label class="form-label fw-bold">Source</label>
          <input class="form-control mb-2" [(ngModel)]="draft.source" />
          <label class="form-label fw-bold">Label</label>
          <input class="form-control mb-2" [(ngModel)]="draft.label" />
          <label class="form-label fw-bold">Severity</label>
          <input type="number" step="0.01" class="form-control mb-2" [(ngModel)]="draft.severity" />
          <label class="form-label fw-bold">Start (ISO)</label>
          <input class="form-control mb-2" [(ngModel)]="draft.start" />
          <label class="form-label fw-bold">End (ISO)</label>
          <input class="form-control mb-3" [(ngModel)]="draft.end" />
          <button class="btn btn-fx btn-sm" (click)="save()">Save</button>
        </div>
      </div>
      <div class="col-lg-8">
        <div class="fx-card">
          <div class="table-responsive">
            <table class="table align-middle mb-0">
              <thead>
                <tr><th>Source</th><th>Label</th><th>Severity</th><th>Window</th><th>Live</th><th></th></tr>
              </thead>
              <tbody>
                @for (row of rows(); track row.source + row.start) {
                  <tr>
                    <td>{{ row.source }}</td>
                    <td>{{ row.label }}</td>
                    <td>{{ row.severity }}</td>
                    <td class="small">{{ row.start }} → {{ row.end }}</td>
                    <td>@if (row.activeNow) { <span class="badge badge-fx">ACTIVE</span> }</td>
                    <td class="text-end">
                      <button class="btn btn-fx-outline btn-sm" (click)="remove(row)">Delete</button>
                    </td>
                  </tr>
                } @empty {
                  <tr><td colspan="6" style="color: var(--fx-ink-soft)">No calendar windows loaded.</td></tr>
                }
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class RefCalendarPage implements OnInit {
  private readonly http = inject(HttpClient);
  readonly rows = signal<CalRow[]>([]);
  readonly error = signal<string | null>(null);
  readonly banner = signal<string | null>(null);
  draft: CalRow = {
    source: 'FOMC',
    severity: 1.25,
    start: new Date().toISOString(),
    end: new Date(Date.now() + 3_600_000).toISOString(),
    label: 'Desk overlay',
  };

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.error.set(null);
    this.http.get<CalRow[]>(`${API.admin}/api/v1/admin/economic-calendar`).subscribe({
      next: (data) => this.rows.set(data ?? []),
      error: (err) => this.error.set(`Load failed: ${err.message || err.status}`),
    });
  }

  save(): void {
    this.http.put(`${API.admin}/api/v1/admin/economic-calendar`, this.draft).subscribe({
      next: () => {
        this.banner.set('Calendar window saved');
        this.reload();
      },
      error: (err) => this.error.set(`Save failed: ${err.message || err.status}`),
    });
  }

  remove(row: CalRow): void {
    this.http
      .delete(`${API.admin}/api/v1/admin/economic-calendar/${row.source}`, { params: { start: row.start } })
      .subscribe({
        next: () => {
          this.banner.set('Deleted');
          this.reload();
        },
        error: (err) => this.error.set(`Delete failed: ${err.message || err.status}`),
      });
  }
}
