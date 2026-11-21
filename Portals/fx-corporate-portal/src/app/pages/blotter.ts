import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { API } from '../api';

@Component({
  selector: 'app-blotter',
  imports: [FormsModule],
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
      <div>
        <h1 class="h3 mb-1">Multi-LP Depth Blotter</h1>
        <p class="mb-0" style="color: var(--fx-ink-soft)">
          Composite depth (LP identities sanitized) · quote {{ api.quote }}
        </p>
      </div>
      <select class="form-select form-select-sm" style="width: auto" [(ngModel)]="pair">
        <option>EUR/USD</option>
        <option>GBP/USD</option>
        <option>USD/JPY</option>
        <option>AUD/USD</option>
      </select>
    </div>

    <div class="row g-3">
      <div class="col-lg-6">
        <div class="fx-card">
          <h2 class="h5 mb-3">Bids · {{ pair }}</h2>
          @for (lvl of bids; track lvl.level) {
            <div class="d-flex align-items-center gap-3 mb-2">
              <span class="fw-bold" style="width: 5rem">{{ lvl.px }}</span>
              <div class="flex-grow-1">
                <div class="depth-bar" [style.width.%]="lvl.depth"></div>
              </div>
              <span class="small fw-semibold" style="width: 4rem; text-align: right">{{ lvl.size }}</span>
              <span class="badge badge-fx">{{ lvl.lp }}</span>
            </div>
          }
        </div>
      </div>
      <div class="col-lg-6">
        <div class="fx-card">
          <h2 class="h5 mb-3">Offers · {{ pair }}</h2>
          @for (lvl of offers; track lvl.level) {
            <div class="d-flex align-items-center gap-3 mb-2">
              <span class="fw-bold" style="width: 5rem">{{ lvl.px }}</span>
              <div class="flex-grow-1">
                <div class="depth-bar" [style.width.%]="lvl.depth" style="background: linear-gradient(90deg, var(--fx-warn), var(--fx-gold))"></div>
              </div>
              <span class="small fw-semibold" style="width: 4rem; text-align: right">{{ lvl.size }}</span>
              <span class="badge badge-fx">{{ lvl.lp }}</span>
            </div>
          }
        </div>
      </div>
    </div>
  `,
})
export class BlotterPage {
  readonly api = API;
  pair = 'EUR/USD';
  readonly bids = [
    { level: 1, px: '1.08412', size: '8.0M', depth: 90, lp: 'LP-A' },
    { level: 2, px: '1.08410', size: '5.5M', depth: 70, lp: 'LP-B' },
    { level: 3, px: '1.08408', size: '4.0M', depth: 55, lp: 'LP-C' },
    { level: 4, px: '1.08405', size: '3.2M', depth: 40, lp: 'LP-D' },
  ];
  readonly offers = [
    { level: 1, px: '1.08416', size: '7.5M', depth: 88, lp: 'LP-B' },
    { level: 2, px: '1.08418', size: '6.0M', depth: 72, lp: 'LP-A' },
    { level: 3, px: '1.08421', size: '3.8M', depth: 50, lp: 'LP-E' },
    { level: 4, px: '1.08424', size: '2.5M', depth: 35, lp: 'LP-C' },
  ];
}
