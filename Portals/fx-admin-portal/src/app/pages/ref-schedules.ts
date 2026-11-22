import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { API } from '../api';

type ScheduleRow = {
  tierId: string;
  ccyPair: string;
  sessionRegime: string;
  markupBps: number;
  newsMultiplier: number;
  status: string;
  approvedBy?: string;
  purposeNote?: string;
};

const PAGE_SIZE = 8;

const REGIME_LABEL: Record<string, string> = {
  LONDON_NY_OVERLAP: 'London–NY overlap',
  TOKYO: 'Tokyo / Asia hours',
  NY_ROLLOVER: 'NY day-end rollover',
  MACRO_NEWS_HIGH: 'High-impact news window',
};

const TIER_LABEL: Record<string, string> = {
  TIER_1: 'Tier 1 · preferred',
  TIER_2: 'Tier 2 · standard corporate',
  TIER_3: 'Tier 3 · retail / high-touch',
};

@Component({
  selector: 'app-ref-schedules',
  imports: [RouterLink],
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
      <div>
        <h1 class="h3 mb-1">Client markup schedules</h1>
        <p class="mb-0" style="color: var(--fx-ink-soft)">
          Desk policy for how much extra we charge clients by tier, currency pair, and market session —
          so margin holds when liquidity thins or major news prints.
        </p>
      </div>
      <div class="d-flex gap-2">
        <button class="btn btn-fx-outline btn-sm" (click)="reload()">Refresh</button>
        <a class="btn btn-fx btn-sm" routerLink="/reference/schedules/create">Create schedule</a>
      </div>
    </div>

    @if (error()) {
      <div class="alert alert-warning">{{ error() }}</div>
    }
    @if (banner()) {
      <div class="alert alert-success">{{ banner() }}</div>
    }

    <div class="fx-card mb-4">
      <h2 class="h5 mb-2">What you are looking at</h2>
      <p class="mb-3" style="color: var(--fx-ink-soft)">
        Each row is a pricing policy the desk owns: for a given client tier and pair, under a given market
        session, apply this client markup (and news factor when a news window is live). New schedules are
        uncommon — most days you review what is already active and approve rare maker–checker changes.
      </p>
      <div class="row g-3">
        <div class="col-md-4">
          <h3 class="h6 mb-1">Market session</h3>
          <p class="small mb-0" style="color: var(--fx-ink-soft)">
            The trading day-part or stress window in force: deep London–NY overlap, quieter Asia hours,
            thin NY day-end rollover, or a high-impact news window (FOMC, NFP, ECB). The live quote uses
            the schedule that matches the current session.
          </p>
        </div>
        <div class="col-md-4">
          <h3 class="h6 mb-1">Client markup</h3>
          <p class="small mb-0" style="color: var(--fx-ink-soft)">
            Extra bank edge added to the client price, measured in basis points. Roughly: 2.0 bp is about
            $200 of bank margin per $1M traded before any news factor.
          </p>
        </div>
        <div class="col-md-4">
          <h3 class="h6 mb-1">News stress factor</h3>
          <p class="small mb-0" style="color: var(--fx-ink-soft)">
            Extra multiplier on client markup while a high-impact news window is active. Calm markets
            stay near 1.0; news windows typically run 1.25–1.50.
          </p>
        </div>
      </div>
    </div>

    <div class="fx-card mb-4">
      <h2 class="h5 mb-2">High-impact news pricing</h2>
      <p class="small mb-3" style="color: var(--fx-ink-soft)">
        These controls do not change the schedule table. They tell the desk stack to price as if a major
        release is live — so quotes pick the <em>High-impact news window</em> schedules below. Clear the
        control when the window is over. For calendar-driven desk confirmation, use Workflows.
      </p>
      <div class="d-flex flex-wrap gap-2">
        <button class="btn btn-fx-outline btn-sm" (click)="setMacro()">
          Apply high-impact news pricing
        </button>
        <button class="btn btn-fx-outline btn-sm" (click)="clearMacro()">
          Return to normal session pricing
        </button>
      </div>
    </div>

    <div class="fx-card">
      <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
        <div>
          <h2 class="h5 mb-0">Active &amp; pending schedules</h2>
          <p class="small mb-0" style="color: var(--fx-ink-soft)">
            {{ rows().length }} schedules · page {{ page() }} of {{ totalPages() }}
          </p>
        </div>
      </div>
      <div class="table-responsive">
        <table class="table align-middle mb-0">
          <thead>
            <tr>
              <th>Client tier</th>
              <th>Pair</th>
              <th>Market session</th>
              <th>Client markup</th>
              <th>News factor</th>
              <th>Purpose</th>
              <th>Status</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            @for (row of pageRows(); track row.tierId + row.ccyPair + row.sessionRegime) {
              <tr>
                <td>{{ tierLabel(row.tierId) }}</td>
                <td>{{ row.ccyPair }}</td>
                <td>{{ regimeLabel(row.sessionRegime) }}</td>
                <td>{{ row.markupBps }} bp</td>
                <td>× {{ row.newsMultiplier }}</td>
                <td class="small" style="max-width: 22rem; color: var(--fx-ink-soft)">
                  {{ row.purposeNote || '—' }}
                </td>
                <td><span class="badge badge-fx">{{ statusLabel(row.status) }}</span></td>
                <td class="text-end">
                  <button class="btn btn-fx-outline btn-sm" (click)="remove(row)">Remove</button>
                </td>
              </tr>
            } @empty {
              <tr>
                <td colspan="8" style="color: var(--fx-ink-soft)">No schedules loaded.</td>
              </tr>
            }
          </tbody>
        </table>
      </div>
      <div class="d-flex justify-content-end align-items-center gap-2 mt-3">
        <button class="btn btn-fx-outline btn-sm" [disabled]="page() <= 1" (click)="prevPage()">Previous</button>
        <span class="small" style="color: var(--fx-ink-soft)">{{ page() }} / {{ totalPages() }}</span>
        <button
          class="btn btn-fx-outline btn-sm"
          [disabled]="page() >= totalPages()"
          (click)="nextPage()"
        >
          Next
        </button>
      </div>
    </div>
  `,
})
export class RefSchedulesPage implements OnInit {
  private readonly http = inject(HttpClient);
  readonly rows = signal<ScheduleRow[]>([]);
  readonly error = signal<string | null>(null);
  readonly banner = signal<string | null>(null);
  readonly page = signal(1);

  readonly totalPages = computed(() => Math.max(1, Math.ceil(this.rows().length / PAGE_SIZE)));
  readonly pageRows = computed(() => {
    const start = (this.page() - 1) * PAGE_SIZE;
    return this.rows().slice(start, start + PAGE_SIZE);
  });

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.error.set(null);
    this.http.get<ScheduleRow[]>(`${API.admin}/api/v1/admin/spread-schedules`).subscribe({
      next: (data) => {
        this.rows.set(data ?? []);
        if (this.page() > this.totalPages()) {
          this.page.set(this.totalPages());
        }
      },
      error: (err) => this.error.set(`Load failed: ${err.message || err.status}`),
    });
  }

  prevPage(): void {
    this.page.update((p) => Math.max(1, p - 1));
  }

  nextPage(): void {
    this.page.update((p) => Math.min(this.totalPages(), p + 1));
  }

  tierLabel(id: string): string {
    return TIER_LABEL[id] ?? id;
  }

  regimeLabel(code: string): string {
    return REGIME_LABEL[code] ?? code;
  }

  statusLabel(status: string): string {
    if (status === 'PENDING_APPROVAL') return 'Pending checker';
    if (status === 'DRAFT') return 'Draft';
    if (status === 'ACTIVE') return 'Active';
    return status;
  }

  setMacro(): void {
    this.http
      .post(`${API.admin}/api/v1/admin/macro-flag`, { source: 'FOMC', severity: 1.25, ttlSeconds: 3600 })
      .subscribe({
        next: () => {
          this.banner.set('High-impact news pricing is on — quotes use the news-window schedules.');
          this.reload();
        },
        error: (err) => this.error.set(`Could not apply news pricing: ${err.message || err.status}`),
      });
  }

  clearMacro(): void {
    this.http.delete(`${API.admin}/api/v1/admin/macro-flag`).subscribe({
      next: () => {
        this.banner.set('Normal session pricing restored.');
        this.reload();
      },
      error: (err) => this.error.set(`Could not restore normal pricing: ${err.message || err.status}`),
    });
  }

  remove(row: ScheduleRow): void {
    this.http
      .delete(
        `${API.admin}/api/v1/admin/spread-schedules/${row.tierId}/${row.ccyPair}/${row.sessionRegime}`,
      )
      .subscribe({
        next: () => {
          this.banner.set('Schedule removed');
          this.reload();
        },
        error: (err) => this.error.set(`Remove failed: ${err.message || err.status}`),
      });
  }
}
