import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { API } from '../api';

@Component({
  selector: 'app-tracker',
  imports: [FormsModule],
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
      <div>
        <h1 class="h3 mb-1">Transfer Tracker</h1>
        <p class="mb-0" style="color: var(--fx-ink-soft)">
          Rate locked → converted → rail settled · cutoffs from {{ api.qualify }}
        </p>
      </div>
    </div>

    <div class="row g-3">
      <div class="col-lg-4">
        <div class="fx-card">
          <label class="form-label fw-bold">Reference</label>
          <input class="form-control mb-3" [(ngModel)]="ref" />
          <button class="btn btn-fx w-100" (click)="load()">Track</button>
        </div>
      </div>
      <div class="col-lg-8">
        <div class="fx-card">
          <h2 class="h5 mb-3">{{ ref }} · Mumbai remittance</h2>
          @for (m of milestones(); track m.label) {
            <div class="milestone" [class.active]="m.state === 'active'" [class.pending]="m.state === 'pending'">
              <span class="dot"></span>
              <div>
                <div class="fw-bold">{{ m.label }}</div>
                <div class="small" style="color: var(--fx-ink-soft)">{{ m.detail }}</div>
              </div>
            </div>
          }
          @if (helper()) {
            <div class="mt-3 p-3" style="background: #fff8eb; border-radius: 0.75rem; border: 1px solid var(--fx-border)">
              <i class="bi bi-info-circle me-1" style="color: var(--fx-gold)"></i>
              {{ helper() }}
            </div>
          }
        </div>
      </div>
    </div>
  `,
})
export class TrackerPage {
  readonly api = API;
  ref = 'RT-92011';
  readonly milestones = signal<
    { label: string; detail: string; state: 'done' | 'active' | 'pending' }[]
  >([]);
  readonly helper = signal<string | null>(null);

  load(): void {
    this.milestones.set([
      { label: 'Rate locked', detail: 'USD/INR 83.42 · 2h window', state: 'done' },
      { label: 'FX converted', detail: 'Netted retail clip · route-exec', state: 'done' },
      { label: 'CLS / local rail', detail: 'In clearing · beneficiary bank open', state: 'active' },
      { label: 'Settled', detail: 'Funds credited to beneficiary', state: 'pending' },
    ]);
    this.helper.set(
      'Beneficiary bank holiday in Maharashtra tomorrow may add ~1 business day. No action needed on your side.',
    );
  }

  constructor() {
    this.load();
  }
}
