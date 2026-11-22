import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { API } from '../api';

type ScheduleRow = {
  tierId: string;
  ccyPair: string;
  sessionRegime: string;
  markupBps: number;
  newsMultiplier: number;
  status: string;
  approvedBy?: string;
};

@Component({
  selector: 'app-policy-studio',
  imports: [FormsModule],
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
      <div>
        <h1 class="h3 mb-1">Policy Studio · FR-01 Spread Schedules</h1>
        <p class="mb-0" style="color: var(--fx-ink-soft)">
          Maker-checker lifecycle for tier × pair × session markup · admin {{ api.admin }} → quote :8081
        </p>
      </div>
      <div class="d-flex gap-2">
        <button class="btn btn-fx-outline btn-sm" (click)="reload()">Refresh</button>
        <button class="btn btn-fx-outline btn-sm" (click)="setMacro()">Arm MACRO_NEWS</button>
        <button class="btn btn-fx-outline btn-sm" (click)="clearMacro()">Clear macro</button>
      </div>
    </div>

    @if (error()) {
      <div class="alert alert-warning">{{ error() }}</div>
    }
    @if (banner()) {
      <div class="alert alert-success">{{ banner() }}</div>
    }

    <div class="row g-3 mb-3">
      <div class="col-lg-5">
        <div class="fx-card h-100">
          <h2 class="h5 mb-3">Draft / approve schedule</h2>
          <label class="form-label fw-bold">Tier</label>
          <select class="form-select mb-2" [(ngModel)]="draft.tierId">
            <option>TIER_1</option>
            <option>TIER_2</option>
            <option>TIER_3</option>
          </select>
          <label class="form-label fw-bold">Pair</label>
          <input class="form-control mb-2" [(ngModel)]="draft.ccyPair" />
          <label class="form-label fw-bold">Session regime</label>
          <select class="form-select mb-2" [(ngModel)]="draft.sessionRegime">
            <option>LONDON_NY_OVERLAP</option>
            <option>TOKYO</option>
            <option>NY_ROLLOVER</option>
            <option>MACRO_NEWS_HIGH</option>
          </select>
          <label class="form-label fw-bold">markup_bps</label>
          <input type="number" step="0.0001" class="form-control mb-2" [(ngModel)]="draft.markupBps" />
          <label class="form-label fw-bold">news_multiplier</label>
          <input type="number" step="0.01" class="form-control mb-3" [(ngModel)]="draft.newsMultiplier" />
          <div class="d-flex flex-wrap gap-2">
            <button class="btn btn-fx-outline btn-sm" (click)="draftRow()">Save draft</button>
            <button class="btn btn-fx-outline btn-sm" (click)="submitRow()">Submit checker</button>
            <button class="btn btn-fx btn-sm" (click)="approveRow()">Approve · activate</button>
          </div>
        </div>
      </div>
      <div class="col-lg-7">
        <div class="fx-card h-100">
          <h2 class="h5 mb-3">Active / pending schedules</h2>
          <div class="table-responsive">
            <table class="table align-middle mb-0">
              <thead>
                <tr>
                  <th>Tier</th>
                  <th>Pair</th>
                  <th>Regime</th>
                  <th>Markup</th>
                  <th>News×</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                @for (row of rows(); track row.tierId + row.ccyPair + row.sessionRegime) {
                  <tr>
                    <td>{{ row.tierId }}</td>
                    <td>{{ row.ccyPair }}</td>
                    <td>{{ row.sessionRegime }}</td>
                    <td>{{ row.markupBps }}</td>
                    <td>{{ row.newsMultiplier }}</td>
                    <td><span class="badge badge-fx">{{ row.status }}</span></td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class PolicyStudioPage implements OnInit {
  private readonly http = inject(HttpClient);
  readonly api = API;
  readonly rows = signal<ScheduleRow[]>([]);
  readonly error = signal<string | null>(null);
  readonly banner = signal<string | null>(null);

  draft: ScheduleRow = {
    tierId: 'TIER_2',
    ccyPair: 'EURUSD',
    sessionRegime: 'NY_ROLLOVER',
    markupBps: 2.0,
    newsMultiplier: 1.25,
    status: 'DRAFT',
    approvedBy: 'DESK_MAKER',
  };

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.error.set(null);
    this.http.get<ScheduleRow[]>(`${API.admin}/api/v1/admin/spread-schedules`).subscribe({
      next: (data) => this.rows.set(data ?? []),
      error: (err) => this.error.set(`Load failed: ${err.message || err.status}`),
    });
  }

  draftRow(): void {
    this.post('draft', { ...this.draft, approvedBy: 'DESK_MAKER' });
  }

  submitRow(): void {
    this.post('submit', { ...this.draft, approvedBy: 'DESK_MAKER' });
  }

  approveRow(): void {
    this.post('approve', { ...this.draft, approvedBy: 'DESK_CHECKER' });
  }

  setMacro(): void {
    this.http
      .post(`${API.admin}/api/v1/admin/macro-flag`, { source: 'FOMC', severity: 1.25, ttlSeconds: 3600 })
      .subscribe({
        next: () => {
          this.banner.set('MACRO_NEWS_HIGH armed on quote via admin');
          this.reload();
        },
        error: (err) => this.error.set(`Macro arm failed: ${err.message || err.status}`),
      });
  }

  clearMacro(): void {
    this.http.delete(`${API.admin}/api/v1/admin/macro-flag`).subscribe({
      next: () => {
        this.banner.set('Macro flag cleared');
        this.reload();
      },
      error: (err) => this.error.set(`Macro clear failed: ${err.message || err.status}`),
    });
  }

  private post(action: 'draft' | 'submit' | 'approve', body: ScheduleRow): void {
    this.error.set(null);
    this.http.post<ScheduleRow>(`${API.admin}/api/v1/admin/spread-schedules/${action}`, body).subscribe({
      next: () => {
        this.banner.set(`Schedule ${action} ok`);
        this.reload();
      },
      error: (err) => this.error.set(`${action} failed: ${err.message || err.status}`),
    });
  }
}
