import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { API } from '../api';

@Component({
  selector: 'app-planner',
  imports: [FormsModule],
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
      <div>
        <h1 class="h3 mb-1">Recurring Remittance Planner</h1>
        <p class="mb-0" style="color: var(--fx-ink-soft)">
          DCA schedules netted into wholesale clips · {{ api.routeExec }}
        </p>
      </div>
      <button class="btn btn-coral btn-sm" (click)="save()">Save plan</button>
    </div>

    <div class="row g-3">
      <div class="col-lg-6">
        <div class="fx-card">
          <label class="form-label fw-bold">Goal</label>
          <select class="form-select mb-3" [(ngModel)]="goal">
            <option>Family support</option>
            <option>Tuition payments</option>
            <option>Overseas mortgage</option>
            <option>Travel wallet top-up</option>
          </select>
          <label class="form-label fw-bold">Amount (USD)</label>
          <input type="number" class="form-control mb-3" [(ngModel)]="amount" />
          <label class="form-label fw-bold">Cadence</label>
          <select class="form-select mb-3" [(ngModel)]="cadence">
            <option>Weekly</option>
            <option>Bi-weekly</option>
            <option>Monthly</option>
          </select>
          <label class="form-label fw-bold">Destination currency</label>
          <select class="form-select" [(ngModel)]="ccy">
            <option>INR</option>
            <option>MXN</option>
            <option>GBP</option>
            <option>EUR</option>
          </select>
        </div>
      </div>
      <div class="col-lg-6">
        <div class="fx-card h-100">
          <h2 class="h5 mb-3">Plan preview</h2>
          <p style="color: var(--fx-ink-soft)">
            {{ amount }} USD → {{ ccy }} · {{ cadence.toLowerCase() }} · goal: {{ goal }}
          </p>
          <ul class="mb-3">
            <li>Converts during London/NY overlap for tighter spreads</li>
            <li>Netted with other retail flow via route-exec</li>
            <li>Optional target-rate pause if mid moves against you</li>
          </ul>
          @if (saved()) {
            <span class="badge badge-fx">Plan saved (demo) — next run Mon 13:00 ET</span>
          }
        </div>
      </div>
    </div>
  `,
})
export class PlannerPage {
  readonly api = API;
  goal = 'Family support';
  amount = 800;
  cadence = 'Monthly';
  ccy = 'INR';
  readonly saved = signal(false);

  save(): void {
    this.saved.set(true);
  }
}
