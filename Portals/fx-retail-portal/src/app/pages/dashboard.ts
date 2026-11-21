import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink],
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
      <div>
        <h1 class="h3 mb-1">Welcome back</h1>
        <p class="mb-0" style="color: var(--fx-ink-soft)">
          Send abroad with institutional-grade rates — no hidden wire fees.
        </p>
      </div>
      <div class="d-flex gap-2">
        <a routerLink="/convert" class="btn btn-coral btn-sm">Convert now</a>
        <a routerLink="/advisor" class="btn btn-fx-outline btn-sm">Ask advisor</a>
      </div>
    </div>

    <div class="row g-3 mb-4">
      @for (kpi of kpis; track kpi.label) {
        <div class="col-md-3">
          <div class="fx-card fx-kpi h-100">
            <span class="label">{{ kpi.label }}</span>
            <span class="value">{{ kpi.value }}</span>
            <span class="small" style="color: var(--fx-accent)">{{ kpi.hint }}</span>
          </div>
        </div>
      }
    </div>

    <div class="row g-3">
      <div class="col-lg-7">
        <div class="fx-card h-100">
          <h2 class="h5 mb-3">Recent transfers</h2>
          <div class="table-responsive">
            <table class="table align-middle mb-0">
              <thead>
                <tr>
                  <th>To</th>
                  <th>Amount</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                @for (t of transfers; track t.id) {
                  <tr>
                    <td class="fw-semibold">{{ t.to }}</td>
                    <td>{{ t.amount }}</td>
                    <td><span class="badge badge-fx">{{ t.status }}</span></td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        </div>
      </div>
      <div class="col-lg-5">
        <div class="fx-card h-100">
          <h2 class="h5 mb-3">Quick actions</h2>
          <a routerLink="/planner" class="btn btn-fx w-100 mb-2">Set recurring remittance</a>
          <a routerLink="/tracker" class="btn btn-fx-outline w-100 mb-2">Track a transfer</a>
          <a routerLink="/convert" class="btn btn-fx-outline w-100">Compare all-in rates</a>
        </div>
      </div>
    </div>
  `,
})
export class DashboardPage {
  readonly kpis = [
    { label: 'USD→INR today', value: '83.42', hint: 'All-in · locked 2h' },
    { label: 'Saved vs bank', value: '$18.40', hint: 'On last $2,500' },
    { label: 'Active plans', value: '2', hint: 'Tuition · family' },
    { label: 'In flight', value: '1', hint: 'CLS rail' },
  ];
  readonly transfers = [
    { id: 1, to: 'Mumbai · Family', amount: '$2,500 → ₹208,550', status: 'Settled' },
    { id: 2, to: 'London · Rent', amount: '£1,800', status: 'Converted' },
    { id: 3, to: 'Mexico City', amount: '$800 → MXN', status: 'Rate locked' },
  ];
}
