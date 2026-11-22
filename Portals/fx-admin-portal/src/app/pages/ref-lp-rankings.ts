import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { API } from '../api';

type RankRow = {
  lpId: string;
  rank: number;
  ruleVersion: string;
  enabled: boolean;
};

@Component({
  selector: 'app-ref-lp-rankings',
  imports: [FormsModule],
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
      <div>
        <h1 class="h3 mb-1">Reference · LP Rankings</h1>
        <p class="mb-0" style="color: var(--fx-ink-soft)">
          Route-exec venue preference seed/config · admin → route-exec :8083
        </p>
      </div>
      <button class="btn btn-fx-outline btn-sm" (click)="reload()">Refresh</button>
    </div>
    @if (error()) { <div class="alert alert-warning">{{ error() }}</div> }
    @if (banner()) { <div class="alert alert-success">{{ banner() }}</div> }
    <div class="row g-3">
      <div class="col-lg-4">
        <div class="fx-card">
          <h2 class="h5 mb-3">Upsert ranking</h2>
          <label class="form-label fw-bold">LP</label>
          <input class="form-control mb-2" [(ngModel)]="draft.lpId" />
          <label class="form-label fw-bold">Rank</label>
          <input type="number" class="form-control mb-2" [(ngModel)]="draft.rank" />
          <label class="form-label fw-bold">Rule version</label>
          <input class="form-control mb-2" [(ngModel)]="draft.ruleVersion" />
          <div class="form-check mb-3">
            <input class="form-check-input" type="checkbox" [(ngModel)]="draft.enabled" id="en" />
            <label class="form-check-label" for="en">Enabled</label>
          </div>
          <button class="btn btn-fx btn-sm" (click)="save()">Save</button>
        </div>
      </div>
      <div class="col-lg-8">
        <div class="fx-card">
          <div class="table-responsive">
            <table class="table align-middle mb-0">
              <thead>
                <tr><th>Rank</th><th>LP</th><th>Rule</th><th>Enabled</th><th></th></tr>
              </thead>
              <tbody>
                @for (row of rows(); track row.lpId) {
                  <tr>
                    <td>{{ row.rank }}</td>
                    <td>{{ row.lpId }}</td>
                    <td><code>{{ row.ruleVersion }}</code></td>
                    <td>{{ row.enabled ? 'Y' : 'N' }}</td>
                    <td class="text-end">
                      <button class="btn btn-fx-outline btn-sm" (click)="remove(row)">Delete</button>
                    </td>
                  </tr>
                } @empty {
                  <tr><td colspan="5" style="color: var(--fx-ink-soft)">No LP rankings.</td></tr>
                }
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class RefLpRankingsPage implements OnInit {
  private readonly http = inject(HttpClient);
  readonly rows = signal<RankRow[]>([]);
  readonly error = signal<string | null>(null);
  readonly banner = signal<string | null>(null);
  draft: RankRow = { lpId: 'LP_CITI', rank: 1, ruleVersion: 'LP_SCORE_v1.0', enabled: true };

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.error.set(null);
    this.http.get<RankRow[]>(`${API.admin}/api/v1/admin/lp-rankings`).subscribe({
      next: (data) => this.rows.set(data ?? []),
      error: (err) => this.error.set(`Load failed: ${err.message || err.status}`),
    });
  }

  save(): void {
    this.http.put(`${API.admin}/api/v1/admin/lp-rankings`, this.draft).subscribe({
      next: () => {
        this.banner.set('Ranking saved');
        this.reload();
      },
      error: (err) => this.error.set(`Save failed: ${err.message || err.status}`),
    });
  }

  remove(row: RankRow): void {
    this.http.delete(`${API.admin}/api/v1/admin/lp-rankings/${row.lpId}`).subscribe({
      next: () => {
        this.banner.set('Deleted');
        this.reload();
      },
      error: (err) => this.error.set(`Delete failed: ${err.message || err.status}`),
    });
  }
}
