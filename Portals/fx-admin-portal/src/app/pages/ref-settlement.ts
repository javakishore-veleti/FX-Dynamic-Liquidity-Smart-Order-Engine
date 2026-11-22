import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { API } from '../api';

type PolicyRow = {
  ccyPair: string;
  tenor: string;
  clsEligible: boolean;
  cutoffTimeUtc: string;
  minClipUsd: number;
  maxClipUsd: number;
  restrictedFlag: boolean;
};

@Component({
  selector: 'app-ref-settlement',
  imports: [FormsModule],
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
      <div>
        <h1 class="h3 mb-1">Reference · Settlement Policies</h1>
        <p class="mb-0" style="color: var(--fx-ink-soft)">
          Qualify cutoffs / clip / restricted · admin → qualify :8082
        </p>
      </div>
      <button class="btn btn-fx-outline btn-sm" (click)="reload()">Refresh</button>
    </div>
    @if (error()) { <div class="alert alert-warning">{{ error() }}</div> }
    @if (banner()) { <div class="alert alert-success">{{ banner() }}</div> }
    <div class="row g-3">
      <div class="col-lg-4">
        <div class="fx-card">
          <h2 class="h5 mb-3">Upsert policy</h2>
          <label class="form-label fw-bold">Pair</label>
          <input class="form-control mb-2" [(ngModel)]="draft.ccyPair" />
          <label class="form-label fw-bold">Tenor</label>
          <input class="form-control mb-2" [(ngModel)]="draft.tenor" />
          <label class="form-label fw-bold">Cutoff UTC</label>
          <input class="form-control mb-2" [(ngModel)]="draft.cutoffTimeUtc" placeholder="23:55:00" />
          <label class="form-label fw-bold">Min clip USD</label>
          <input type="number" class="form-control mb-2" [(ngModel)]="draft.minClipUsd" />
          <label class="form-label fw-bold">Max clip USD</label>
          <input type="number" class="form-control mb-2" [(ngModel)]="draft.maxClipUsd" />
          <div class="form-check mb-2">
            <input class="form-check-input" type="checkbox" [(ngModel)]="draft.clsEligible" id="cls" />
            <label class="form-check-label" for="cls">CLS eligible</label>
          </div>
          <div class="form-check mb-3">
            <input class="form-check-input" type="checkbox" [(ngModel)]="draft.restrictedFlag" id="rst" />
            <label class="form-check-label" for="rst">Restricted</label>
          </div>
          <button class="btn btn-fx btn-sm" (click)="save()">Save</button>
        </div>
      </div>
      <div class="col-lg-8">
        <div class="fx-card">
          <div class="table-responsive">
            <table class="table align-middle mb-0">
              <thead>
                <tr>
                  <th>Pair</th><th>Tenor</th><th>Cutoff</th><th>Clip</th><th>CLS</th><th>Restricted</th><th></th>
                </tr>
              </thead>
              <tbody>
                @for (row of rows(); track row.ccyPair + row.tenor) {
                  <tr>
                    <td>{{ row.ccyPair }}</td>
                    <td>{{ row.tenor }}</td>
                    <td>{{ row.cutoffTimeUtc }}</td>
                    <td class="small">{{ row.minClipUsd }} – {{ row.maxClipUsd }}</td>
                    <td>{{ row.clsEligible ? 'Y' : 'N' }}</td>
                    <td>{{ row.restrictedFlag ? 'Y' : 'N' }}</td>
                    <td class="text-end">
                      <button class="btn btn-fx-outline btn-sm" (click)="remove(row)">Delete</button>
                    </td>
                  </tr>
                } @empty {
                  <tr><td colspan="7" style="color: var(--fx-ink-soft)">No settlement policies seeded.</td></tr>
                }
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class RefSettlementPage implements OnInit {
  private readonly http = inject(HttpClient);
  readonly rows = signal<PolicyRow[]>([]);
  readonly error = signal<string | null>(null);
  readonly banner = signal<string | null>(null);
  draft: PolicyRow = {
    ccyPair: 'EURUSD',
    tenor: 'SPOT',
    clsEligible: true,
    cutoffTimeUtc: '23:55:00',
    minClipUsd: 100000,
    maxClipUsd: 50000000,
    restrictedFlag: false,
  };

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.error.set(null);
    this.http.get<PolicyRow[]>(`${API.admin}/api/v1/admin/settlement-policies`).subscribe({
      next: (data) => this.rows.set(data ?? []),
      error: (err) => this.error.set(`Load failed: ${err.message || err.status}`),
    });
  }

  save(): void {
    this.http.put(`${API.admin}/api/v1/admin/settlement-policies`, this.draft).subscribe({
      next: () => {
        this.banner.set('Policy saved');
        this.reload();
      },
      error: (err) => this.error.set(`Save failed: ${err.message || err.status}`),
    });
  }

  remove(row: PolicyRow): void {
    this.http.delete(`${API.admin}/api/v1/admin/settlement-policies/${row.ccyPair}/${row.tenor}`).subscribe({
      next: () => {
        this.banner.set('Deleted');
        this.reload();
      },
      error: (err) => this.error.set(`Delete failed: ${err.message || err.status}`),
    });
  }
}
