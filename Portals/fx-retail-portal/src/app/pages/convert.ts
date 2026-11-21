import { Component, computed, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { API } from '../api';

@Component({
  selector: 'app-convert',
  imports: [FormsModule],
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
      <div>
        <h1 class="h3 mb-1">All-In Rate Visualizer</h1>
        <p class="mb-0" style="color: var(--fx-ink-soft)">
          Transparent mid vs bank markup · quote edge {{ api.quote }}
        </p>
      </div>
    </div>

    <div class="row g-3">
      <div class="col-lg-5">
        <div class="fx-card">
          <label class="form-label fw-bold">You send (USD)</label>
          <input type="number" class="form-control form-control-lg mb-3" [(ngModel)]="amount" (ngModelChange)="recalc()" />
          <label class="form-label fw-bold">They receive</label>
          <select class="form-select mb-3" [(ngModel)]="toCcy" (ngModelChange)="recalc()">
            <option>INR</option>
            <option>MXN</option>
            <option>GBP</option>
            <option>EUR</option>
          </select>
          <div class="fx-kpi mb-3">
            <span class="label">Recipient gets</span>
            <span class="value">{{ receive() }}</span>
          </div>
          <button class="btn btn-coral w-100">Lock rate for 2 hours</button>
        </div>
      </div>
      <div class="col-lg-7">
        <div class="fx-card h-100">
          <h2 class="h5 mb-3">Rate breakdown</h2>
          <div class="mb-3">
            <div class="d-flex justify-content-between small fw-bold mb-1">
              <span>Mid-market</span>
              <span>{{ mid() }}</span>
            </div>
            <div class="rate-bar mb-3" style="background: #b7e0e8; width: 100%"></div>
            <div class="d-flex justify-content-between small fw-bold mb-1">
              <span>Typical retail bank</span>
              <span>{{ bankRate() }}</span>
            </div>
            <div class="rate-bar mb-3" style="background: var(--fx-coral); width: 72%"></div>
            <div class="d-flex justify-content-between small fw-bold mb-1">
              <span>Our SOR all-in</span>
              <span>{{ ourRate() }}</span>
            </div>
            <div class="rate-bar" style="background: var(--fx-accent); width: 92%"></div>
          </div>
          <div class="d-flex flex-wrap gap-2">
            <span class="badge badge-fx">Wire fee: $0</span>
            <span class="badge badge-coral">You save ~{{ savings() }}</span>
            <span class="badge badge-fx">Session: London/NY</span>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class ConvertPage {
  readonly api = API;
  amount = 2500;
  toCcy = 'INR';
  private readonly rates: Record<string, { mid: number; our: number; bank: number }> = {
    INR: { mid: 83.55, our: 83.42, bank: 81.9 },
    MXN: { mid: 17.12, our: 17.05, bank: 16.6 },
    GBP: { mid: 0.792, our: 0.789, bank: 0.775 },
    EUR: { mid: 0.921, our: 0.918, bank: 0.905 },
  };

  readonly tick = signal(0);

  readonly mid = computed(() => {
    this.tick();
    return this.rates[this.toCcy].mid.toFixed(4);
  });
  readonly ourRate = computed(() => {
    this.tick();
    return this.rates[this.toCcy].our.toFixed(4);
  });
  readonly bankRate = computed(() => {
    this.tick();
    return this.rates[this.toCcy].bank.toFixed(4);
  });
  readonly receive = computed(() => {
    this.tick();
    const r = this.rates[this.toCcy].our * this.amount;
    return `${r.toLocaleString(undefined, { maximumFractionDigits: 2 })} ${this.toCcy}`;
  });
  readonly savings = computed(() => {
    this.tick();
    const r = this.rates[this.toCcy];
    const save = (r.our - r.bank) * this.amount;
    return `$${Math.abs(save).toFixed(2)}`;
  });

  recalc(): void {
    this.tick.update((n) => n + 1);
  }
}
