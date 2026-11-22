import { Component, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { API } from '../api';

type ScheduleDraft = {
  tierId: string;
  ccyPair: string;
  sessionRegime: string;
  markupBps: number;
  newsMultiplier: number;
  purposeNote: string;
  approvedBy?: string;
};

@Component({
  selector: 'app-ref-schedules-create',
  imports: [FormsModule, RouterLink],
  template: `
    <div class="mb-3">
      <a class="small" routerLink="/reference/schedules" style="color: var(--fx-ink-soft)">← Back to schedules</a>
    </div>

    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
      <div>
        <h1 class="h3 mb-1">Create client markup schedule</h1>
        <p class="mb-0" style="color: var(--fx-ink-soft)">
          Infrequent desk policy change. Capture purpose so the checker knows what margin or risk outcome
          this schedule is protecting.
        </p>
      </div>
    </div>

    @if (error()) {
      <div class="alert alert-warning">{{ error() }}</div>
    }
    @if (banner()) {
      <div class="alert alert-success">{{ banner() }}</div>
    }

    <div class="row g-3">
      <div class="col-lg-7">
        <div class="fx-card">
          <h2 class="h5 mb-3">Schedule details</h2>

          <label class="form-label fw-bold">Client tier</label>
          <select class="form-select mb-1" [(ngModel)]="draft.tierId">
            <option value="TIER_1">Tier 1 · preferred / franchise</option>
            <option value="TIER_2">Tier 2 · standard corporate</option>
            <option value="TIER_3">Tier 3 · retail / high-touch</option>
          </select>
          <p class="small mb-3" style="color: var(--fx-ink-soft)">
            Who this policy applies to. Preferred clients keep tighter markup; retail pays more for coverage.
          </p>

          <label class="form-label fw-bold">Currency pair</label>
          <input class="form-control mb-1" [(ngModel)]="draft.ccyPair" />
          <p class="small mb-3" style="color: var(--fx-ink-soft)">
            Example: EURUSD. One schedule per tier, pair, and market session.
          </p>

          <label class="form-label fw-bold">Market session</label>
          <select class="form-select mb-1" [(ngModel)]="draft.sessionRegime">
            <option value="LONDON_NY_OVERLAP">London–NY overlap — deepest liquidity</option>
            <option value="TOKYO">Tokyo / Asia hours — thinner EURUSD</option>
            <option value="NY_ROLLOVER">NY day-end rollover — thin books, higher reject risk</option>
            <option value="MACRO_NEWS_HIGH">High-impact news window — FOMC / NFP / ECB</option>
          </select>
          <p class="small mb-3" style="color: var(--fx-ink-soft)">
            Which trading day-part or stress window this policy covers. Live pricing selects the schedule
            that matches the session in force — so we stay competitive in deep liquidity and defend
            margin when books thin or news hits.
          </p>

          <label class="form-label fw-bold">Client markup (basis points)</label>
          <input type="number" step="0.0001" class="form-control mb-1" [(ngModel)]="draft.markupBps" />
          <p class="small mb-3" style="color: var(--fx-ink-soft)">
            Extra bank edge on the client price. About $100 per $1M notional for each basis point on a
            spot pair. Higher markup = more margin protection; lower markup = more aggressive client price.
          </p>

          <label class="form-label fw-bold">News stress factor</label>
          <input type="number" step="0.01" class="form-control mb-1" [(ngModel)]="draft.newsMultiplier" />
          <p class="small mb-3" style="color: var(--fx-ink-soft)">
            Multiplier on client markup while a high-impact news window is active. Use 1.0 for calm
            sessions; typically 1.25–1.50 for news. Effective edge ≈ markup × this factor.
          </p>

          <label class="form-label fw-bold">Purpose</label>
          <textarea
            class="form-control mb-1"
            rows="3"
            maxlength="500"
            [(ngModel)]="draft.purposeNote"
            placeholder="Why does this schedule exist? What margin or risk outcome are we protecting?"
          ></textarea>
          <p class="small mb-4" style="color: var(--fx-ink-soft)">
            Required for checker review — e.g. “Widen Tier-2 EURUSD at NY rollover to protect margin in thin books.”
          </p>

          <div class="d-flex flex-wrap gap-2">
            <button class="btn btn-fx-outline btn-sm" (click)="save('draft')">Save draft</button>
            <button class="btn btn-fx-outline btn-sm" (click)="save('submit')">Submit for checker</button>
            <button class="btn btn-fx btn-sm" (click)="save('approve')">Approve &amp; activate</button>
            <a class="btn btn-fx-outline btn-sm" routerLink="/reference/schedules">Cancel</a>
          </div>
        </div>
      </div>

      <div class="col-lg-5">
        <div class="fx-card">
          <h2 class="h5 mb-3">Maker–checker</h2>
          <ol class="small mb-0 ps-3" style="color: var(--fx-ink-soft)">
            <li class="mb-2"><strong>Save draft</strong> — park a proposal without going live.</li>
            <li class="mb-2"><strong>Submit for checker</strong> — peer / risk review.</li>
            <li class="mb-2"><strong>Approve &amp; activate</strong> — schedule becomes live for quoting.</li>
          </ol>
        </div>
        <div class="fx-card mt-3">
          <h2 class="h5 mb-2">What we are trying to achieve</h2>
          <p class="small mb-0" style="color: var(--fx-ink-soft)">
            Keep bank margin and reduce adverse flow in thin or news-driven sessions, while still offering
            competitive prices to preferred clients when liquidity is deep — without a release cycle.
          </p>
        </div>
      </div>
    </div>
  `,
})
export class RefSchedulesCreatePage {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  readonly error = signal<string | null>(null);
  readonly banner = signal<string | null>(null);

  draft: ScheduleDraft = {
    tierId: 'TIER_2',
    ccyPair: 'EURUSD',
    sessionRegime: 'NY_ROLLOVER',
    markupBps: 2.0,
    newsMultiplier: 1.25,
    purposeNote: '',
  };

  save(action: 'draft' | 'submit' | 'approve'): void {
    this.error.set(null);
    if (!this.draft.purposeNote?.trim()) {
      this.error.set('Add a purpose so the checker knows why this schedule exists.');
      return;
    }
    const body = {
      ...this.draft,
      approvedBy: action === 'approve' ? 'DESK_CHECKER' : 'DESK_MAKER',
    };
    this.http.post(`${API.admin}/api/v1/admin/spread-schedules/${action}`, body).subscribe({
      next: () => {
        this.banner.set(
          action === 'approve'
            ? 'Schedule activated'
            : action === 'submit'
              ? 'Submitted for checker'
              : 'Draft saved',
        );
        setTimeout(() => this.router.navigateByUrl('/reference/schedules'), 600);
      },
      error: (err) => this.error.set(`${action} failed: ${err.message || err.status}`),
    });
  }
}
