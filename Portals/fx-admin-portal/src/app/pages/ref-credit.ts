import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { API } from '../api';

type CreditRow = {
  clientId: string;
  lpId: string;
  maxCreditUsd: number;
  utilizedUsd: number;
  reservedUsd: number;
  resetEpoch?: number;
};

@Component({
  selector: 'app-ref-credit',
  imports: [FormsModule],
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
      <div>
        <h1 class="h3 mb-1">Reference · Credit Lines</h1>
        <p class="mb-0" style="color: var(--fx-ink-soft)">
          Bilateral credit seed/config · admin → qualify :8082
        </p>
      </div>
      <button class="btn btn-fx-outline btn-sm" (click)="reload()">Refresh</button>
    </div>
    @if (error()) { <div class="alert alert-warning">{{ error() }}</div> }
    @if (banner()) { <div class="alert alert-success">{{ banner() }}</div> }
    <div class="row g-3">
      <div class="col-lg-4">
        <div class="fx-card">
          <h2 class="h5 mb-3">Upsert line</h2>
          <label class="form-label fw-bold">Client</label>
          <input class="form-control mb-2" [(ngModel)]="draft.clientId" />
          <label class="form-label fw-bold">LP</label>
          <input class="form-control mb-2" [(ngModel)]="draft.lpId" />
          <label class="form-label fw-bold">Max USD</label>
          <input type="number" class="form-control mb-2" [(ngModel)]="draft.maxCreditUsd" />
          <label class="form-label fw-bold">Utilized</label>
          <input type="number" class="form-control mb-2" [(ngModel)]="draft.utilizedUsd" />
          <label class="form-label fw-bold">Reserved</label>
          <input type="number" class="form-control mb-3" [(ngModel)]="draft.reservedUsd" />
          <button class="btn btn-fx btn-sm" (click)="save()">Save</button>
        </div>
      </div>
      <div class="col-lg-8">
        <div class="fx-card">
          <div class="table-responsive">
            <table class="table align-middle mb-0">
              <thead>
                <tr><th>Client</th><th>LP</th><th>Max</th><th>Utilized</th><th>Reserved</th><th></th></tr>
              </thead>
              <tbody>
                @for (row of rows(); track row.clientId + row.lpId) {
                  <tr>
                    <td>{{ row.clientId }}</td>
                    <td>{{ row.lpId }}</td>
                    <td>{{ row.maxCreditUsd }}</td>
                    <td>{{ row.utilizedUsd }}</td>
                    <td>{{ row.reservedUsd }}</td>
                    <td class="text-end">
                      <button class="btn btn-fx-outline btn-sm" (click)="remove(row)">Delete</button>
                    </td>
                  </tr>
                } @empty {
                  <tr><td colspan="6" style="color: var(--fx-ink-soft)">No credit lines — seed runs on qualify startup / first qualify.</td></tr>
                }
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class RefCreditPage implements OnInit {
  private readonly http = inject(HttpClient);
  readonly rows = signal<CreditRow[]>([]);
  readonly error = signal<string | null>(null);
  readonly banner = signal<string | null>(null);
  draft: CreditRow = {
    clientId: 'CORP_001',
    lpId: 'LP_CITI',
    maxCreditUsd: 25_000_000,
    utilizedUsd: 0,
    reservedUsd: 0,
  };

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.error.set(null);
    this.http.get<CreditRow[]>(`${API.admin}/api/v1/admin/credit-lines`).subscribe({
      next: (data) => this.rows.set(data ?? []),
      error: (err) => this.error.set(`Load failed: ${err.message || err.status}`),
    });
  }

  save(): void {
    this.http.put(`${API.admin}/api/v1/admin/credit-lines`, this.draft).subscribe({
      next: () => {
        this.banner.set('Credit line saved');
        this.reload();
      },
      error: (err) => this.error.set(`Save failed: ${err.message || err.status}`),
    });
  }

  remove(row: CreditRow): void {
    this.http.delete(`${API.admin}/api/v1/admin/credit-lines/${row.clientId}/${row.lpId}`).subscribe({
      next: () => {
        this.banner.set('Deleted');
        this.reload();
      },
      error: (err) => this.error.set(`Delete failed: ${err.message || err.status}`),
    });
  }
}
