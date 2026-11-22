import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { API } from '../api';

type LpRow = {
  ccyPair: string;
  lpId: string;
  bid: number;
  ask: number;
  availableSize: number;
  quoteTs?: number;
  ttlMs?: number;
};

@Component({
  selector: 'app-ref-lp-book',
  imports: [FormsModule],
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
      <div>
        <h1 class="h3 mb-1">Reference · LP Top-of-Book</h1>
        <p class="mb-0" style="color: var(--fx-ink-soft)">
          Quote-service seed/config book · admin → quote :8081
        </p>
      </div>
      <button class="btn btn-fx-outline btn-sm" (click)="reload()">Refresh</button>
    </div>
    @if (error()) { <div class="alert alert-warning">{{ error() }}</div> }
    @if (banner()) { <div class="alert alert-success">{{ banner() }}</div> }
    <div class="row g-3">
      <div class="col-lg-4">
        <div class="fx-card">
          <h2 class="h5 mb-3">Upsert quote</h2>
          <label class="form-label fw-bold">Pair</label>
          <input class="form-control mb-2" [(ngModel)]="draft.ccyPair" />
          <label class="form-label fw-bold">LP</label>
          <input class="form-control mb-2" [(ngModel)]="draft.lpId" />
          <label class="form-label fw-bold">Bid</label>
          <input type="number" step="0.00001" class="form-control mb-2" [(ngModel)]="draft.bid" />
          <label class="form-label fw-bold">Ask</label>
          <input type="number" step="0.00001" class="form-control mb-2" [(ngModel)]="draft.ask" />
          <label class="form-label fw-bold">Size</label>
          <input type="number" class="form-control mb-3" [(ngModel)]="draft.availableSize" />
          <button class="btn btn-fx btn-sm" (click)="save()">Save</button>
        </div>
      </div>
      <div class="col-lg-8">
        <div class="fx-card">
          <div class="table-responsive">
            <table class="table align-middle mb-0">
              <thead>
                <tr><th>Pair</th><th>LP</th><th>Bid</th><th>Ask</th><th>Size</th><th></th></tr>
              </thead>
              <tbody>
                @for (row of rows(); track row.ccyPair + row.lpId) {
                  <tr>
                    <td>{{ row.ccyPair }}</td>
                    <td>{{ row.lpId }}</td>
                    <td>{{ row.bid }}</td>
                    <td>{{ row.ask }}</td>
                    <td>{{ row.availableSize }}</td>
                    <td class="text-end">
                      <button class="btn btn-fx-outline btn-sm" (click)="remove(row)">Delete</button>
                    </td>
                  </tr>
                } @empty {
                  <tr><td colspan="6" style="color: var(--fx-ink-soft)">No LP quotes — trigger a quote or upsert above.</td></tr>
                }
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class RefLpBookPage implements OnInit {
  private readonly http = inject(HttpClient);
  readonly rows = signal<LpRow[]>([]);
  readonly error = signal<string | null>(null);
  readonly banner = signal<string | null>(null);
  draft: LpRow = { ccyPair: 'EURUSD', lpId: 'LP_CITI', bid: 1.0848, ask: 1.085, availableSize: 5_000_000 };

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.error.set(null);
    this.http.get<LpRow[]>(`${API.admin}/api/v1/admin/lp-book`).subscribe({
      next: (data) => this.rows.set(data ?? []),
      error: (err) => this.error.set(`Load failed: ${err.message || err.status}`),
    });
  }

  save(): void {
    this.http.put(`${API.admin}/api/v1/admin/lp-book`, this.draft).subscribe({
      next: () => {
        this.banner.set('LP quote saved');
        this.reload();
      },
      error: (err) => this.error.set(`Save failed: ${err.message || err.status}`),
    });
  }

  remove(row: LpRow): void {
    this.http.delete(`${API.admin}/api/v1/admin/lp-book/${row.ccyPair}/${row.lpId}`).subscribe({
      next: () => {
        this.banner.set('Deleted');
        this.reload();
      },
      error: (err) => this.error.set(`Delete failed: ${err.message || err.status}`),
    });
  }
}
